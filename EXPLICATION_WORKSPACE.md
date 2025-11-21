
post {--
    always {
        cleanWs()  // ❌ Supprime le workspace après chaque build
    }
}


post {
    success {
        echo 'Pipeline réussi avec succès!'
    }
    failure {
        echo 'Pipeline a échoué!'
    }
    // cleanWs() supprimé - le workspace est conservé
}



post {
    success {
        echo 'Pipeline réussi avec succès!'
    }
    failure {
        echo 'Pipeline a échoué!'
        cleanWs()  // Nettoie seulement en cas d'échec
    }
}





post {
    success {
        echo 'Pipeline réussi avec succès!'
    }
    failure {
        echo 'Pipeline a échoué!'
    }
    cleanup {
        // Nettoie seulement si le build est plus ancien que 7 jours
        script {
            def buildAge = currentBuild.getDuration()
            if (buildAge > 7 * 24 * 60 * 60 * 1000) {
                cleanWs()
            }
        }
    }
}