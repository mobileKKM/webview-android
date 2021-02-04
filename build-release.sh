#!/bin/bash

echo "Building app with versionCode latest"

if [ ! -f keystore.properties ]; then
    echo "No keystore configuration file found! Abort..."
    exit
fi

bash ./gradlew app:assembleRelease
status=$?

if [ ! $status -eq 0 ]; then
    echo "Build failed, please check gradle output for details."
    exit
fi

echo "Uploading build..."

cp app/build/outputs/apk/release/app-release.apk mobileKKM_latest.apk
cp app/build/outputs/mapping/release/mapping.txt proguard.txt

curl -F chat_id="101110325" -F disable_notification="false" -F document=@"mobileKKM_latest.apk" -F caption="Building app with versionCode latest" https://api.telegram.org/bot$BOT_TOKEN/sendDocument
curl -F chat_id="101110325" -F disable_notification="false" -F document=@"proguard.txt" https://api.telegram.org/bot$BOT_TOKEN/sendDocument

echo "Done! Build successfully uploaded via Telegram."
