def call(Map pipelineParams) {
def rtMaven = Artifactory.newMavenBuild()
def server = Artifactory.server 'server1'
node {
	   try {
		   stage('Downloading Deployments')
		   {
			println "Checking"
			def downloadSpec = """{
			   "files": [
			    {
				"pattern": " Gradle_Test/Android/GGK-21/unspecified/*.apk",
				"target": "artifactories/"
			    }
			]
			}"""
			server.download(downloadSpec)
		   }
		   stage('Deployments') {
			sh """ scp ./SpringMVCSecurityXML/target/SpringMVCSecurityXML.war ${pipelineParams.remote_user}@${pipelineParams.remote_ip}:${pipelineParams.remote_location} """
		   }
	   }
	   catch(Exception e)
	   {
		println "IN Catch Block"
	   }
      }
}
	
