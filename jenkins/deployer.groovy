kubectl_image = "egovio/kubectl:0.0.2"

def takeSnapshot(group, env){
    stage("Snapshot ${env} env"){
        def cmd = "python jenkins/scripts/snapshot.py ${group}"
        run(env, cmd)
    }
}

def deploy(env){
    stage("Deploy to ${env} env"){
        def cmd = "python jenkins/scripts/deploy.py"
        run(env, cmd)
    }
}

def deployStandAlone(env, service, tag){
    stage("Deploy to ${env} env"){
        def cmd = "python cluster/apply.py  -e dev -m ${service} -i egovio/${service}:${tag} -dmi egovio/${service}-db:${tag} -d"
        run(env, cmd)
    }
}

def run(env, cmd){
    docker.image("${kubectl_image}").inside {
        set_kube_credentials(env)
        withCredentials([string(credentialsId: "${env}-kube-url", variable: "KUBE_SERVER_URL")]){
            sh "kubectl config set-cluster env --server ${KUBE_SERVER_URL}"
        }
        sh cmd;
    }
}

def set_kube_credentials(env){
    withCredentials([file(credentialsId: "${env}-kube-ca", variable: "CA")]){
        sh "cp ${CA} /kube/ca.pem"
    }
    withCredentials([file(credentialsId: "${env}-kube-cert", variable: "CERT")]){
        sh "cp ${CERT} /kube/admin.pem"
    }
    withCredentials([file(credentialsId: "${env}-kube-key", variable: "CERT_KEY")]){
        sh "cp ${CERT_KEY} /kube/admin-key.pem"
    }
}

return this;
