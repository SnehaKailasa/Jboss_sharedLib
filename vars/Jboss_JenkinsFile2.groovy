def call(Map pipelineParams) {
def rtMaven = Artifactory.newMavenBuild()
def buildInfo = null
def server = null

node {
		try {
			cleanWs()
			stage ('Source Code Checkout') {
				Reason = "GIT Checkout Failed"
				checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/SnehaKailasa23/Java_sample_app.git']]]
				//checkout scm
			}
			server =  Artifactory.server pipelineParams.ArtifactoryServerName
			stage('Maven Build') {
				Reason = "Maven Build Failed"
				rtMaven.deployer server: server, snapshotRepo: pipelineParams.snapshot_repo, releaseRepo: pipelineParams.release_repo			//Deploying artifacts to this repo //
				rtMaven.deployer.deployArtifacts = false		//this will not publish artifacts soon after build succeeds	//						
				rtMaven.tool = 'maven'	
				// Maven build starts here //
				def mvn_version = tool 'maven'
				withEnv( ["PATH+MAVEN=${mvn_version}/bin"]  ) {
					buildInfo = rtMaven.run pom: 'SpringMVCSecurityXML/pom.xml', goals: 'clean install -Dmaven.test.skip=true' 
				}
			}
			
			stage('Docker-Compose and RFW'){
        			Reason = "Docker Compose Or RFW Failed"
				sh """ sudo docker-compose up -d
				ls -al
				sudo chmod 744 wait_for_robot.sh
				sudo chmod 744 clean_up.sh
				ls -al
				./wait_for_robot.sh """
				step([$class: 'RobotPublisher',
					outputPath: "/home/robot/results",
					passThreshold: 0,
					unstableThreshold: 0,
					otherFiles: ""])
				// If Robot Framework test case fails, then the build will be failed //	
				if("${currentBuild.result}" == "FAILURE"){	
					sh ''' exit 1 '''
				}
			}
      
      			stage ('Pushing Artifacts'){	
				Reason = "Artifacts Deployment Failed"
				rtMaven.deployer.deployArtifacts buildInfo
			  	server.publishBuildInfo buildInfo
			}
			
			/*stage('Deployments') {
				sh """ scp ./SpringMVCSecurityXML/target/SpringMVCSecurityXML.war ${pipelineParams.remote_user}@${pipelineParams.remote_ip}:${pipelineParams.remote_location} """
			}*/
			
			stage('Triggering CD Job') {
				build job: 'JBoss_CD_Job', wait: false
			}
      
      			stage ('Email Notifications') {
				properties([[$class: 'EnvInjectJobProperty', info: [loadFilesFromMaster: false, propertiesContent: "JobWorkSpace=${WORKSPACE}"], keepBuildVariables: true, keepJenkinsSystemVariables: true, on: true]])
				emailext (
					attachLog: true, attachmentsPattern: '*.html, output.xml', body: '''
					${SCRIPT, template="email_template_success.groovy"}''', subject: '$DEFAULT_SUBJECT', to: "${pipelineParams.success_recipients}") 
			}
		}
		catch(Exception e)
		{
			properties([[$class: 'EnvInjectJobProperty', info: [loadFilesFromMaster: false, propertiesContent: "JobWorkSpace=${WORKSPACE}"], keepBuildVariables: true, keepJenkinsSystemVariables: true, on: true]])
				emailext (
					attachLog: true, attachmentsPattern: '*.html, output.xml', body: '''
					${SCRIPT, template="email_template_failure.groovy"}''', subject: '$DEFAULT_SUBJECT', to: "${pipelineParams.failed_recipients}") 
			sh 'exit 1'
		}
		finally
		{
			sh './clean_up.sh'
		}
	}
}
