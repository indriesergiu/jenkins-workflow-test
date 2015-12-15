node('linux') {
    stage 'checkout'
    def checkout = performCheckout()
    stage 'prepare'
    def prepare = prepareBuild()
    stage 'build'
    catchError {
        withEnv(["PATH=${tool 'JavaSDK1.8.0_45'}/bin:${tool 'Maven 3.2.2'}/bin:${env.PATH}", "MAVEN_OPTS=-Xmx1024M -XX:MaxPermSize=256m"]) {
            sh 'mvn -s settings.xml -Dskip.embedded.mongo=false clean install'
            step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml, '])
            sh 'mvn -s settings.xml clean build-helper:remove-project-artifact'
        }
    }

    step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'sergiu.indrie@iquestgroup.com marius.gherman@iquestgroup.com', sendToIndividuals: true])
}

def performCheckout() {
//    git branch: 'dev', credentialsId: 'service.mobile.key.credentials', url: 'ssh://git@stash.iquestgroup.com/mr/rooms-server.git'
}

def prepareBuild() {
    writeFile encoding: 'UTF-8', file: 'settings.xml', text: '<?xml version="1.0"?><settings xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/SETTINGS/1.0.0"><localRepository>${env.PWD}/.repository</localRepository><servers><server><id>mr-releases</id><username>mr.dev</username><password>mr.dev</password></server><server><id>mr-thirdparty</id><username>mr.dev</username><password>mr.dev</password></server><server><id>mr-snapshots</id><username>mr.dev</username><password>mr.dev</password></server></servers></settings>'
}