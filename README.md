# Kotlin-Weather-App
The final project in this set, it takes the database implementation, adds permission managing, and takes the original front end to make a weather app. The weather app checks for permissions to access GPS and requests them if they are not present. Upon gaining them, it then checks the openweather api for the current weather for your location, as well as the forecast for the nest 15 hours. It also checks UMSL's weather for the same time window. It will display a warning upon a detection of severe weather conditions, and it also has a google map display in the bottom right hand corner which shows current location. Aside from this map though, all framework components are kept away from any google play services dependencies to make the app as non-reliant on google services as possible.

## Running 
- Open Android Studio and import the project once you have it cloned. 
- Either look at the code or pack it into an apk or run it on an AVD
