#!/bin/sh

set -eu

mkdir -p "$HOME/.sbt/0.13"
mkdir -p "$HOME/.sbt/1.0"

cat << EOF > "$HOME/.sbt/.credentials"
realm=Artifactory Realm
host=repo.powerspace.com
user=$1
password=$2
EOF

cat << EOF > "$HOME/.sbt/1.0/global.sbt"
credentials += Credentials(Path.userHome / ".sbt"  / ".credentials")

publishMavenStyle := true

publishTo := {
  val artifactory = "https://repo.powerspace.com/artifactory"
    if (isSnapshot.value)
      Some("snapshots" at artifactory + "/libs-snapshot-local")
    else
      Some("releases" at artifactory + "/libs-release-local")
  }
EOF
cp "$HOME/.sbt/1.0/global.sbt" "$HOME/.sbt/0.13/"
