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
				"pattern": "fortna_snapshot/*.war",
				"target": "artifactories/"
			    }
			]
			}"""
			server.download(downloadSpec)
			sh ''' ls ./artifactories '''
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
	
