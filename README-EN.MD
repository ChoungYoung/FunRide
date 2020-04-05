# FunRide
Fun Ride is a navigation application developed by kotlin.

## Main Function

When entering the app, the user will be asked for location permission, 
and when the map information is not available, the user will be guided to open GPS for location.

After the user clicks to start, a service will be started to calculate the user's use time and walking route distance, 
and the most suitable route will be planned along with the user's movement. 
During the use process, the weak GPS signal will notify the user.

The end of the trip will shows the user's path map, time spent and route distance.

The app adaptive for English and Simplied-Chinese language

## Libraries

The project is developed based on the baidu map SDK, using the locationing and map functions of Baidu map. 
In the development process, the **ViewModel**、 **LiveData** and **Room** in the jetpack library are used.

The Room database is used together with the Kotlin Coroutine to save this itinerary for follow-up display. 
The **Constraintlayout** used in the layout xml file to reduces the layout hierarchy.
The page is displayed by fragment and shares ViewModel data.
