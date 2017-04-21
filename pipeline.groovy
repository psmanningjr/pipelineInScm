#!/usr/bin/env groovy

//input message: 'message', ok: 'do it', parameters: [choice(choices: "red\ngreen\npurple\n'], description: '', name: 'color')
stuff = input message: 'Select Promotion Parameters', 
parameters: [
  choice(choices: "mint-dev\nmint-test\nmint-stage\nmint-system-integration\nmint-training\nmint-prod", description: '', name: 'from'),
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
  sh "echo ${stuff}"
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
  
  sh "echo config_repo = ${CONFIG_REPO}"
  sh "echo to= ${TO_NAMESPACE}"
//CONF_REPO = stuff[configRepo]
//CONF_BRANCH = stuff[CONFIG_BRANCH]
 //  def mvnHome
 //  stage('Preparation') { // for display purposes
 //     // Get some code from a GitHub repository
 //     //git 'https://github.com/jglick/simple-maven-project-with-tests.git'
 //     git branch: 'master', credentialsId: 'mint-dev-jenkinsgitlabsecret', url:'git@10.127.183.7:openshift/dev-configs.git' 
 //     // Get the Maven tool.
 //     // ** NOTE: This 'M3' Maven tool must be configured
 //     // **       in the global configuration.   
 //    //mvnHome = tool 'maven'
 //     sh "ls -tal"
 //     }
 //  }
  
  //node('maven') {
   //  sh "echo ${stuff}"

     //sh "echo"
    
    //sh "ls -tal"
   // checkout scm
   // sh "ls -tal"
    //scmdump = scm.dump()
    //sh 'echo scm dump = ${scmdump}'
   // sh 'mvn --version'
  //}
  //node('master') {
   
  // sh "ls -tal"
   //sh "ls -tal /tmp"
    dir( 'config_repo' ) { 
      git branch: CONFIG_BRANCH, credentialsId: 'mint-dev-jenkinsgitlabsecret', url: CONFIG_REPO
        sh 'ls -tal'
    }
    
    dir ( 'app_repo' ) { 
      git branch: APP_BRANCH, credentialsId: 'mint-dev-jenkinsgitlabsecret', url: APP_REPO
        sh 'ls -tal'
     }

  sh "oc process $TEMPLATE_NAME -n syngenta RUNTIME=$RUNTIME HOSTNAME_HTTP=$HOSTNAME_HTTP | oc apply -f - -n $TO_NAMESPACE"
  sh "echo to= ${TO_NAMESPACE}"
  sh "oc project ${TO_NAMESPACE}"
//# Get parameters expected by template
//dir ( 'app_repo' ) { 
//  sh "oc process --namespace ${TO_NAMESPACE} -f openshift-config-map-template.yml --parameters"
//}
def params = sh returnStdout:true, script: "oc process --namespace ${TO_NAMESPACE} -f app_repo/openshift-config-map-template.yml --parameters | cut -f 1 -d "+'" "' + " | tail -n +2"
//  TEMPLATE_PARAMS= (oc process --namespace ${TO_NAMESPACE} -f app_repo/openshift-config-map-template.yml --parameters | cut -f 1 -d &quot; &quot; | tail -n +2).execute.text
  
 // sh " echo ${TEMPLATE_PARAMS}"
  //# Filter out unneeded config arguments
// org TEMPLATE_ARGS=$(for item in $TEMPLATE_PARAMS; do printf &quot;$(grep ^$item= config_repo/vars.sh) &quot;; done)
//  TEMPLATE_ARGS= sh "for item in $TEMPLATE_PARAMS; do printf &quot; returnitem(item) &quot;; done"
//  echo "oc process --namespace=${TO_NAMESPACE} -f app_repo/openshift-config-map-template.yml ${TEMPLATE_ARGS} | oc apply -f - --namespace=$TO_NAMESPACE"
//echo "oc tag $FROM_NAMESPACE/$APP_NAME:$FROM_TAG $TO_NAMESPACE/$APP_NAME:latest"
}

returnitem (item) {
  sh "grep '^${item}=' config_repo/vars.sh"
}
