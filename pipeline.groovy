#!/usr/bin/env groovy

//input message: 'message', ok: 'do it', parameters: [choice(choices: "red\ngreen\npurple\n'], description: '', name: 'color')
stuff = input message: 'Select Promotion Parameters', 
parameters: [
  choice(choices: "mint-dev\nmint-test\nmint-stage\nmint-system-integration\nmint-training\nmint-prod", description: '', name: 'from'),
  choice(choices: "mint-dev\nmint-test\nmint-stage\nmint-system-integration\nmint-training\nmint-prod", description: '', name: 'to'),
  choice(choices: "git@10.127.183.7:mint/mint-integration.git\ngit@10.127.183.7:mint/identity.git\ngit@10.127.183.7:mint/material.git\ngit@10.127.183.7:mint/mint-security-admin.git", description: '', name: 'appRepo'),
  choice(choices: "git@10.127.183.7:openshift/dev-configs.git\ngit@10.127.183.7:openshift/test-configs.git\ngit@10.127.183.7:openshift/stage-configs.git\ngit@10.127.183.7:openshift/system-integration-configs.git\ngit@10.127.183.7:openshift/training-configs.git\ngit@10.127.183.7:openshift/prod-configs.git", description: '', name: 'configRepo'),
  choice(choices: "mint-integration\nidentity\nmaterial\nmint-system-integration\nmint-security", description: '', name: 'templateName'),
  string(defaultValue: '', description: '', name: 'HOSTNAME_HTTP'),
  string(defaultValue: 'QA', description: '', name: 'RUNTIME'),
  string(defaultValue: 'QA', description: '', name: 'APP_BRANCH'),
  string(defaultValue: 'QA', description: '', name: 'CONFIG_BRANCH'),
  choice(choices: "integration\nidentity\nmaterial\nsecurity", description: '', name: 'app_name')

  ]

 //node {
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
  node('master') {
   
  // sh "ls -tal"
   //sh "ls -tal /tmp"
    dir( 'config_repo' ) { sh 'pwd'
    }
    
    dir path: 'app_repo' { sh 'pwd'
                 }
//  <scm class="org.jenkinsci.plugins.multiplescms.MultiSCM" plugin="multiple-scms@0.6">
//    <scms>
//      <hudson.plugins.git.GitSCM plugin="git@3.0.0">
//        <configVersion>2</configVersion>
//        <userRemoteConfigs>
//          <hudson.plugins.git.UserRemoteConfig>
//            <name>config_repo</name>
//            <url>${CONFIG_REPO}</url>
//            <credentialsId>mint-dev-jenkinsgitlabsecret</credentialsId>
//          </hudson.plugins.git.UserRemoteConfig>
//        </userRemoteConfigs>
//        <branches>
//          <hudson.plugins.git.BranchSpec>
//            <name>${CONFIG_BRANCH}</name>
//          </hudson.plugins.git.BranchSpec>
//        </branches>
//        <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
//        <submoduleCfg class="list"/>
//        <extensions>
//          <hudson.plugins.git.extensions.impl.ScmName>
//            <name>config_repo</name>
//          </hudson.plugins.git.extensions.impl.ScmName>
//          <hudson.plugins.git.extensions.impl.RelativeTargetDirectory>
//            <relativeTargetDir>config_repo</relativeTargetDir>
//          </hudson.plugins.git.extensions.impl.RelativeTargetDirectory>
//        </extensions>
//      </hudson.plugins.git.GitSCM>
//      <hudson.plugins.git.GitSCM plugin="git@3.0.0">
//        <configVersion>2</configVersion>
//        <userRemoteConfigs>
//          <hudson.plugins.git.UserRemoteConfig>
//            <name>app_repo</name>
//            <url>${APP_REPO}</url>
//            <credentialsId>mint-dev-jenkinsgitlabsecret</credentialsId>
//          </hudson.plugins.git.UserRemoteConfig>
//        </userRemoteConfigs>
//        <branches>
//          <hudson.plugins.git.BranchSpec>
//            <name>${APP_BRANCH}</name>
//          </hudson.plugins.git.BranchSpec>
//        </branches>
//        <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
//        <submoduleCfg class="list"/>
//        <extensions>
//          <hudson.plugins.git.extensions.impl.ScmName>
//            <name>app_repo</name>
//          </hudson.plugins.git.extensions.impl.ScmName>
//          <hudson.plugins.git.extensions.impl.RelativeTargetDirectory>
//            <relativeTargetDir>app_repo</relativeTargetDir>
//          </hudson.plugins.git.extensions.impl.RelativeTargetDirectory>
//        </extensions>
//      </hudson.plugins.git.GitSCM>
//    </scms>
//  </scm>
//  <canRoam>true</canRoam>
//  <disabled>false</disabled>
//  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
//  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
//  <triggers/>
//  <concurrentBuild>false</concurrentBuild>
//  <builders>
//    <hudson.tasks.Shell>
//      <command>ls app_repo
//ls config_repo
//oc process $TEMPLATE_NAME -n syngenta RUNTIME=$RUNTIME HOSTNAME_HTTP=$HOSTNAME_HTTP | oc apply -f - -n $TO_NAMESPACE
//oc project $TO_NAMESPACE
//# Get parameters expected by template
//TEMPLATE_PARAMS=$(oc process --namespace $TO_NAMESPACE -f app_repo/openshift-config-map-template.yml --parameters | cut -f 1 -d &quot; &quot; | tail -n +2)
//# Filter out unneeded config arguments
//TEMPLATE_ARGS=$(for item in $TEMPLATE_PARAMS; do printf &quot;$(grep ^$item= config_repo/vars.sh) &quot;; done)
//oc process --namespace=$TO_NAMESPACE -f app_repo/openshift-config-map-template.yml $TEMPLATE_ARGS | oc apply -f - --namespace=$TO_NAMESPACE
//oc tag $FROM_NAMESPACE/$APP_NAME:$FROM_TAG $TO_NAMESPACE/$APP_NAME:latest </command>
//    </hudson.tasks.Shell>
}
