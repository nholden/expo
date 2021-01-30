#!/usr/bin/env bash

# Early exit when script is run on platforms other than macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
  exit 0
fi

green="tput setaf 2"
yellow="tput setaf 3"
blue="tput setaf 4"
reset="tput sgr0"

HEADER_PRINTED=false

check-cocoapods-locks() {
  podfileLockPath="$1/Podfile.lock"
  manifestLockPath="$1/Pods/Manifest.lock"
  podfileLock=`md5 -q $podfileLockPath 2>&1`
  manifestLock=`md5 -q $manifestLockPath 2>&1`

  if [ "$podfileLock" != "$manifestLock" ]; then
    if [ $HEADER_PRINTED = false ]; then
      printf "\\n⚠️  $($yellow)Your local pods are outdated, please run $($blue)pod install$($yellow) in the following directories:$($reset)\\n"
      HEADER_PRINTED=true
    fi
    printf "👉 $($green)$1$($reset)\\n"
  fi
}

check-cocoapods-locks ios
check-cocoapods-locks apps/bare-expo/ios
