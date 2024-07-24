# SignalConso antivirus API

Api qui permet de scanner les piéces jointes des utilisateurs de signal conso.

Signal conso doit fonctionner même lorsque l'antivirus est indisponible, cependant le téléchargement des piéces jointes par les pros et agents sera bloqué si la piéce jointe n'a pas été scannée.

Plus d'information ici : https://beta.gouv.fr/startup/signalement.html

L'API nécessite une base PostgreSQL pour la persistence des données (versions supportées : 14+).

Le build se fait à l'aide de [SBT](https://www.scala-sbt.org/) (voir [build.sbt])

### Variables d'environnement

| Nom                                 | Description                                                                                                                                                     | Valeur par défaut |
|:------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------|
| APPLICATION_SECRET                  | Clé secrète de l'application                                                                                                                                    |                   |
| SENTRY_DSN                          | Identifiant pour intégration avec [Sentry](https://sentry.io)                                                                                                   |                   |
| TMP_DIR                             | Dossier temporaire qui sert de tampon pour la génération des fichiers / import de fichiers                                                                      |                   |
| POSTGRESQL_ADDON_URI                | Full database url                                                                                                                                               |                   |
| POSTGRESQL_ADDON_HOST               | Database host                                                                                                                                                   |                   |
| POSTGRESQL_ADDON_PORT               | Database port                                                                                                                                                   |                   |
| POSTGRESQL_ADDON_DB                 | Database name                                                                                                                                                   |                   |
| POSTGRESQL_ADDON_USER               | Database user                                                                                                                                                   |                   |
| POSTGRESQL_ADDON_PASSWORD           | Database password                                                                                                                                               |                   |
| MAX_CONNECTIONS                     | Max connection (hikari property)                                                                                                                                |                   |
| NUM_THREADS                         | Thread count used to process db connections (hikari property)                                                                                                   |                   |
| AUTHENTICATION_TOKEN                | Client Authentication token used to call the apis                                                                                                               | false             |
| S3_ACCESS_KEY_ID                    | ID du compte S3 utilisé                                                                                                                                         |                   |
| S3_SECRET_ACCESS_KEY                | SECRET du compte S3 utilisé                                                                                                                                     |                   |
| S3_ENDPOINT_URL                     | host du bucket                                                                                                                                                  |                   |
| BUCKETS_REPORT                      | nom du bucket                                                                                                                                                   |                   |
| CC_CLAMAV                           | Flag activation antivirus sur la machine clever cloud (minimum 2Go de ram), obligatoire pour le bon fonctionnement du service                                   | true              |
| USE_TEXT_LOGS                       | Si true, les logs seront au format texte (plus lisible pour travailler en local) plutôt que JSON (qui est le format en prod, pour que New Relic les parse bien) |                   |

# Développement

### PostgreSQL

L'application requiert une connexion à un serveur PostgreSQL (sur macOS, vous pouvez
utiliser [https://postgresapp.com/]).
Créez une base de données pour l'application : `createdb signalconso` (par défaut localement, la base sera accessible au
user `$USER`, sans mot de passe).

Il est possible de lancer un PostgreSQL à partir d'une commande docker-compose (le fichier en question est disponible
sous scripts/local/)

à la racine du projet faire :

```
docker-compose -f scripts/local/docker-compose.yml up

```

L'application utilise un schéma différent de celui de l'api principale signal conso et ne doit pas partager la même base de données.

#### Script de migration

Le projet utilise l'outil flyway (https://flywaydb.org/) pour la gestion des scripts de migration.

Les scripts de migration sont lancés au run de l'application, ils sont disponibles dans le repertoire conf/db/migration/default.

**Warning** Important

Un script doit impérativement être écrit de manière à ce que l'application fonctionne toujours en cas de rollback de l'application.

Ceci afin de ne pas avoir gérer de procédure de rollback complexe :
Avoir l'ancienne la structure de données et la nouvelle qui fonctionnent en parralèle puis un certain temps après supprimer l'ancienne structure.

Cette méthode est recommandée par flyway et est décrite sur le lien suivant : https://documentation.red-gate.com/fd/rollback-guidance-138347143.html



### Configuration locale

Lancer une base de donnes PosgreSQL provisionée avec les tables et données (voir plus haut)

L'application a besoin de variables d'environnements. Vous devez les configurer. Il y a plusieurs manières de faire,
nous recommandons la suivante :

```bash
# à ajouter dans votre .zprofile, .zshenv .bash_profile, ou équivalent
# pour toutes les valeurs avec XXX, vous devez renseigner des vraies valeurs.
# Vous pouvez par exemple reprendre les valeurs de l'environnement de démo dans Clever Cloud

function scsbt {
  # Set all environnements variables for the api then launch sbt
  # It forwards arguments, so you can do "scsbt", "scscbt compile", etc.
  echo "Launching sbt with extra environnement variables"
  AUTHENTICATION_TOKEN="XXX" \
  TMP_DIR="/tmp/" \
  S3_ACCESS_KEY_ID="XXX" \
  S3_SECRET_ACCESS_KEY="XXX" \
  POSTGRESQL_ADDON_URI="XXX" \
  USE_TEXT_LOGS="true" \
  POSTGRESQL_ADDON_HOST="XXX" \
  POSTGRESQL_ADDON_PORT="XXX" \
  POSTGRESQL_ADDON_DB="XXX" \
  POSTGRESQL_ADDON_USER="XXX" \
  POSTGRESQL_ADDON_PASSWORD= \
  sbt "$@"
}

```

Ou si vous utilisez fish shell :

```fish
# à ajouter dans votre fish.config
# pour toutes les valeurs avec XXX, vous devez renseigner des vraies valeurs.
# Vous pouvez par exemple reprendre les valeurs de l'environnement de démo dans Clever Cloud

function scsbt
  # Set all environnements variables for the api then launch sbt
  # It forwards arguments, so you can do "scsbt", "scscbt compile", etc.
  echo "Launching sbt with extra environnement variables"
  set -x AUTHENTICATION_TOKEN "XXX"
  set -x TMP_DIR "/tmp/"
  set -x S3_ACCESS_KEY_ID "XXX"
  set -x S3_SECRET_ACCESS_KEY "XXX"
  set -x POSTGRESQL_ADDON_URI "XXX"
  set -x USE_TEXT_LOGS "true"
  set -x POSTGRESQL_ADDON_HOST "XXX"
  set -x POSTGRESQL_ADDON_PORT "XXX"
  set -x POSTGRESQL_ADDON_DB "XXX"
  set -x POSTGRESQL_ADDON_USER "XXX"
  set -x POSTGRESQL_ADDON_PASSWORD
  sbt $argv
end
```

Pour lancer les tests uniquement, renseigner simplement les valeurs suivantes :

| Variable                  | Valeur           |
|:--------------------------|:-----------------|
| POSTGRESQL_ADDON_HOST     | localhost        |
| POSTGRESQL_ADDON_PORT     | 5432             |
| POSTGRESQL_ADDON_DB       | test_signalconso |
| POSTGRESQL_ADDON_USER     | $USER            |
| POSTGRESQL_ADDON_PASSWORD |                  |


Ceci définit une commande `scsbt`, à utiliser à la place de `sbt`

#### ❓ Pourquoi définir cette fonction, pourquoi ne pas juste exporter les variables en permanence ?

Pour éviter que ces variables ne soient lisibles dans l'environnement par n'importe quel process lancés sur votre
machine. Bien sûr c'est approximatif, on ne peut pas empêcher un process de parser le fichier de conf directement, mais
c'est déjà un petit niveau de protection.

#### ❓ Puis-je mettre ces variables dans un fichier local dans le projet, que j'ajouterai au .gitignore ?

C'est trop dangereux. Nos repos sont publics, la moindre erreur humaine au niveau du .gitignore pourrait diffuser toutes
les variables.

### Lancer l'appli

Lancer

```bash
scsbt run
```

L'API est accessible à l'adresse `http://localhost:9000/api` avec rechargement à chaud des modifications.

## Tests

Pour exécuter les tests :

```bash
scsbt test
```

Pour éxecuter uniquement un test (donné par son nom de classe):

```bash
scsbt "testOnly *SomeTestSpec"
```

### Gestion des logs

Procédure pour pousser les logs sur new relic :

```
clever login
clever link --org orga_ID app_ID
#  You should see application alias displayed
clever applications
#To drain log to new relic
clever drain create --alias "CLEVER_CLOUD_ALIAS" NewRelicHTTP "https://log-api.eu.newrelic.com/log/v1" --api-key NEW_RELIC_API_KEY
clever applications
```

### Restauration de de base de données

Des backups sont disponibles pour récupérer la base données en cas de problème.

1. Récupérer le fichier de backup
2. Créer une nouvelle base de données vierge
3. Lancer la commande suivante :

```
#Pour restaurer sur une nouvelle base de données
pg_restore -h $HOST -p $PORT -U $USER -d $DATABASE_NAME --format=c ./path/backup.dump --no-owner --role=$USER --verbose --no-acl --schema=$SIGNAL_CONSO_SCHEMA
```

4. Rattacher la base de données à l'application.


### Gestion de l'authentification

La génération de token se fait via le bout de code suivant :

val clearValueToken = "mon token sécurisé"
Token.hash(Token.ClearToken(clearValueToken))

Le client doit passer dans un header "X-Api-Key" la valeur du token clearValueToken
Ce service doit être initialisé avec pour valeur AUTHENTICATION_TOKEN le résultat de l'expression Token.hash(Token.ClearToken(clearValueToken))