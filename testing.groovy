

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
  
  //println "______________________________________________________________________________________________________"
  //println "    Get ${CONFIG_REPO} Configuration Repo" 
  //dir( 'config_repo' ) { 
  //    git branch: CONFIG_BRANCH, credentialsId: 'mint-dev-jenkinsgitlabsecret', url: CONFIG_REPO
  //    sh 'ls -tal'
  //}
    
  //println "______________________________________________________________________________________________________"
  //println "    Get ${APP_REPO} Application Repo" 
  //dir ( 'app_repo' ) { 
  //    git branch: APP_BRANCH, credentialsId: 'mint-dev-jenkinsgitlabsecret', url: APP_REPO
  //    sh 'ls -tal'
  //}
  
  def result = getPipelineRepo('./')

  println "______________________________________________________________________________________________________"
  println "    Merging list of name to create a list of sets "
  }
  
  stage('Process parameters') {
    sh "cat ./listOfNamesFromTemplate "
  def TEMPLATE_PARAMS = fieldNamesFromTemplateParamsList('./listOfNamesFromTemplate')
  println "Template parameters = ${TEMPLATE_PARAMS}"

  //# Filter out unneeded config arguments
  def setCommands = fileLinesToList('./fileWithEqualsAndBlanks') 
    println "setCommands = ${setCommands}"
  def TEMPLATE_ARGS = buildAssignmentList(TEMPLATE_PARAMS, setCommands)
  println "args = $TEMPLATE_ARGS" 
 }
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
      git branch:onBranch, credentialsId: withCredentialId, url: fromUrl
  }
}

def getPipelineRepo(String toDir){
  dir ( toDir) {
    checkout scm
  }
}
