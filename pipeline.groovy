#!/usr/bin/env groovy

inputParams = input message: 'Select Promotion Parameters', 
parameters: [
  choice(choices: "mint-dev\nmint-test\nmint-stage\nmint-system-integration\nmint-training\nmint-prod", description: '', name: 'from'),
  string(defaultValue: 'latest', description: '', name: 'fromTag'),
  choice(choices: "mint-test\nmint-stage\nmint-system-integration\nmint-training\nmint-prod", description: '', name: 'to_namespace'),
  choice(choices: "git@10.127.183.7:mint/mint-integration.git\ngit@10.127.183.7:mint/identity.git\ngit@10.127.183.7:mint/material.git\ngit@10.127.183.7:mint/mint-security-admin.git", description: '', name: 'appRepo'),
  choice(choices: "git@10.127.183.7:openshift/test-configs.git\ngit@10.127.183.7:openshift/test-configs.git\ngit@10.127.183.7:openshift/stage-configs.git\ngit@10.127.183.7:openshift/system-integration-configs.git\ngit@10.127.183.7:openshift/training-configs.git\ngit@10.127.183.7:openshift/prod-configs.git", description: '', name: 'configRepo'),
  choice(choices: "mint-integration\nidentity\nmaterial\nmint-system-integration\nmint-security", description: '', name: 'templateName'),
  string(defaultValue: 'integration-tst.fed5.syngenta-usae.openshiftapps.com', description: '', name: 'hostnameHTTP'),
  string(defaultValue: 'QA', description: '', name: 'runtime'),
  string(defaultValue: 'openshift', description: '', name: 'appBranch'),
  string(defaultValue: 'master', description: '', name: 'configBranch'),
  choice(choices: "integration\nidentity\nmaterial\nsecurity", description: '', name: 'app_name')

  ]

node {
  stage('Git clone') {
    //sh "echo ${inputParams}"
    FROM_TAG = inputParams.get('fromTag')
    APP_REPO = inputParams.get('appRepo')
    APP_BRANCH = inputParams.get('appBranch')
    CONFIG_REPO = inputParams.get('configRepo')
    CONFIG_BRANCH = inputParams.get('configBranch')
    TEMPLATE_NAME = inputParams.get('templateName')
    RUNTIME = inputParams.get('runtime')
    APP_NAME = inputParams.get('app_name')
    TO_NAMESPACE = inputParams.get('to_namespace')
    FROM_NAMESPACE = inputParams.get('from')
    TEMPLATE_NAME = inputParams.get('templateName')
    HOSTNAME_HTTP = inputParams.get('hostnameHTTP')
  
    println "______________________________________________________________________________________________________"
    println "    Get ${CONFIG_REPO} Configuration Repo" 
    
    getRepo(CONFIG_REPO, CONFIG_BRANCH, 'config_repo', 'mint-dev-jenkinsgitlabsecret') 

    getRepo(APP_REPO, APP_BRANCH, 'app_repo', 'mint-dev-jenkinsgitlabsecret')

    println "______________________________________________________________________________________________________"
    println "    Get ${APP_REPO} Application Repo" 
  }
  
  println "______________________________________________________________________________________________________"
  println "    Create ${TO_NAMESPACE} Deployment configs etc. for ${TEMPLATE_NAME} if needed "

  sh "oc process ${TEMPLATE_NAME} -n syngenta RUNTIME=${RUNTIME} HOSTNAME_HTTP=${HOSTNAME_HTTP} >/tmp/toprocess"
  sh "oc apply -f /tmp/toprocess -n ${TO_NAMESPACE}"
 
  sh "oc project ${TO_NAMESPACE}"
  
  println "______________________________________________________________________________________________________"
  println "    Create/update Configmap in ${TO_NAMESPACE} for ${APP_NAME} "

  //# Get parameters expected by template
  sh script: "oc process --namespace ${TO_NAMESPACE} -f app_repo/openshift-config-map-template.yml --parameters >/tmp/paraminfo "
  def TEMPLATE_PARAMS = fieldNamesFromTemplateParamsList('/tmp/paraminfo')
  println "Template parameters = ${TEMPLATE_PARAMS}"

  //# Filter out unneeded config arguments
  def setCommands = fileLinesToList('config_repo/vars.sh') 
    println "setCommands = ${setCommands}"
  def TEMPLATE_ARGS = buildAssignmentList(TEMPLATE_PARAMS, setCommands)
  //def TEMPLATE_ARGS =""
  //def configVars = readFile('config_repo/vars.sh')
  //String[] splitData = configVars.split("\n");
  //count = splitData.size()
  ////println "config vars = ${configVars}"
  //for (String eachSplit : splitData) {
    //println "processing ${eachSplit}"
  //  indexOfEquals = eachSplit.indexOf("=")
  //  if (indexOfEquals > -1 ) {
  //    compare = eachSplit.substring(0,indexOfEquals)
  //    if(TEMPLATE_PARAMS.contains(compare)){
  //        TEMPLATE_ARGS = TEMPLATE_ARGS + '"' + eachSplit + '" '
  //    }
  //  }
  }
  //println "args = $TEMPLATE_ARGS"
  
  sh "oc process --namespace=${TO_NAMESPACE} -f app_repo/openshift-config-map-template.yml ${TEMPLATE_ARGS} >/tmp/configmap"
  sh "oc apply -f /tmp/configmap --namespace=${TO_NAMESPACE}"

  println "______________________________________________________________________________________________________"
  println "    Promote ${FROM_NAMESPACE} to ${TO_NAMESPACE} for ${APP_NAME}" 
  sh "oc tag ${FROM_NAMESPACE}/${APP_NAME}:${FROM_TAG} ${TO_NAMESPACE}/${APP_NAME}:latest"
}


def fieldNamesFromTemplateParamsList(inputFile) {
  sh "tail -n +2 ${inputFile} | cut -f 1 -d "+'" "' + " >/tmp/onlynames "
  def fieldnamesOnly = readFile('/tmp/onlynames').trim()
  sh "rm -f /tmp/onlynames"
  return fieldnamesOnly
}

def buildAssignmentList(TEMPLATE_PARAMS, setCommands) {
  def assignmentList =""
    for (String setCMD : setCommands) {
      //println "processing ${setCMD}"
      indexOfEquals = setCMD.indexOf("=")
      if (indexOfEquals > -1 ) {
        compare = setCMD.substring(0,indexOfEquals)
        if(TEMPLATE_PARAMS.contains(compare)){
          assignmentList = assignmentList + '"' + setCMD + '" '
        }
      }
    }
  return assignmentList
}
  
def fileLinesToList(inputFile) {
  def configVars = readFile(inputFile)
  String[] list = configVars.split("\n");
  return list
}

def getRepo(String fromURL, String onBranch, String toDir, String withCredentialId) {
  dir ( toDir ) { 
      git branch:onBranch, credentialsId: withCredentialId, url: fromURL
  }
}

def getPipelineRepo(String toDir){
  dir ( toDir) {
    checkout scm
  }
}
