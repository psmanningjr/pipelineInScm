#!/usr/bin/env groovy

stuff = input message: 'Select Promotion Parameters', 
parameters: [
  choice(choices: "mint-dev\nmint-test\nmint-stage\nmint-system-integration\nmint-training\nmint-prod", description: '', name: 'from'),
  string(defaultValue: 'latest', description: '', name: 'fromTag'),
  choice(choices: "mint-test\nmint-stage\nmint-system-integration\nmint-training\nmint-prod", description: '', name: 'to_namespace'),
  choice(choices: "git@10.127.183.7:mint/mint-integration.git\ngit@10.127.183.7:mint/identity.git\ngit@10.127.183.7:mint/material.git\ngit@10.127.183.7:mint/mint-security-admin.git", description: '', name: 'appRepo'),
  choice(choices: "git@10.127.183.7:openshift/test-configs.git\ngit@10.127.183.7:openshift/test-configs.git\ngit@10.127.183.7:openshift/stage-configs.git\ngit@10.127.183.7:openshift/system-integration-configs.git\ngit@10.127.183.7:openshift/training-configs.git\ngit@10.127.183.7:openshift/prod-configs.git", description: '', name: 'configRepo'),
  choice(choices: "mint-integration\nidentity\nmaterial\nmint-system-integration\nmint-security", description: '', name: 'templateName'),
  string(defaultValue: 'integration-tst.fed5.syngenta-usae.openshiftapps.com', description: '', name: 'HOSTNAME_HTTP'),
  string(defaultValue: 'QA', description: '', name: 'RUNTIME'),
  string(defaultValue: 'openshift', description: '', name: 'APP_BRANCH'),
  string(defaultValue: 'master', description: '', name: 'CONFIG_BRANCH'),
  choice(choices: "integration\nidentity\nmaterial\nsecurity", description: '', name: 'app_name')

  ]


node {
  //sh "echo ${stuff}"
  FROM_TAG = stuff.get('fromTag')
  APP_REPO = stuff.get('appRepo')
  APP_BRANCH = stuff.get('APP_BRANCH')
  CONFIG_REPO = stuff.get('configRepo')
  CONFIG_BRANCH = stuff.get('CONFIG_BRANCH')
  TEMPLATE_NAME = stuff.get('templateName')
  RUNTIME = stuff.get('RUNTIME')
  APP_NAME = stuff.get('app_name')
  TO_NAMESPACE = stuff.get('to_namespace')
  FROM_NAMESPACE = stuff.get('from')
  TEMPLATE_NAME = stuff.get('templateName')
  HOSTNAME_HTTP = stuff.get('HOSTNAME_HTTP')
  
  println "______________________________________________________________________________________________________"
  println "    Get ${CONFIG_REPO} Configuration Repo" 
  
  dir( 'config_repo' ) { 
      git branch: CONFIG_BRANCH, credentialsId: 'mint-dev-jenkinsgitlabsecret', url: CONFIG_REPO
      sh 'ls -tal'
  }
    
  println "______________________________________________________________________________________________________"
  println "    Get ${APP_REPO} Application Repo" 
  dir ( 'app_repo' ) { 
      git branch: APP_BRANCH, credentialsId: 'mint-dev-jenkinsgitlabsecret', url: APP_REPO
      sh 'ls -tal'
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
  sh "cut -f 1 -d "+'" "' + " /tmp/paraminfo >/tmp/onlynames"
  sh "tail -n +2 /tmp/onlynames >/tmp/tailed"

  def TEMPLATE_PARAMS = readFile('/tmp/tailed').trim()
  //println "Template parameters = ${TEMPLATE_PARAMS}"

  //# Filter out unneeded config arguments
  def TEMPLATE_ARGS =""
  def configVars = readFile('config_repo/vars.sh')
  String[] splitData = configVars.split("\n");
  count = splitData.size()
  //println "config vars = ${configVars}"
  for (String eachSplit : splitData) {
    //println "processing ${eachSplit}"
    indexOfEquals = eachSplit.indexOf("=")
    if (indexOfEquals > -1 ) {
      compare = eachSplit.substring(0,indexOfEquals)
      if(TEMPLATE_PARAMS.contains(compare)){
          TEMPLATE_ARGS = TEMPLATE_ARGS + '"' + eachSplit + '" '
      }
    }
  }
  //println "args = $TEMPLATE_ARGS"
  
  sh "oc process --namespace=${TO_NAMESPACE} -f app_repo/openshift-config-map-template.yml ${TEMPLATE_ARGS} >/tmp/configmap"
  sh "oc apply -f /tmp/configmap --namespace=${TO_NAMESPACE}"

  println "______________________________________________________________________________________________________"
  println "    Promote ${FROM_NAMESPACE} to ${TO_NAMESPACE} for ${APP_NAME}" 
  sh "oc tag ${FROM_NAMESPACE}/${APP_NAME}:${FROM_TAG} ${TO_NAMESPACE}/${APP_NAME}:latest"
}
