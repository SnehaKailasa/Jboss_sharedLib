def call(Map pipelineParams) {
def rtMaven = Artifactory.newMavenBuild()
node {
   try {
	   stage('')
	   {
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
	   stage('Deployments') 
	   {
		sh """ chmod 777 remote_script.sh 
		ssh -T "${pipelineParams.remote_user}"@"${pipelineParams.remote_ip}" "bash -s" < ./remote_script.sh """
	   }
   }
   catch()
   {
   
   }
}
