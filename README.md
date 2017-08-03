# NotiFires

This file contains an overview of the Code for "Notifires", which is a startup that offers personalized routing with fire notifications.

The lectures from the "Data Analytics in an Internet of Things Context" covered a range of knowledge and materials in terms of Big data and Internet of Things (IoT) solutions. Also, example projects were demonstrated in class on the development of WhatsApp (with Kafka) and the Co2 pollution of car models (with Kafka and Spark).

By going through the lectures and demonstrations done in class students were able to use these contents in order to adapt to use cases created by them in groups as a class project. In our case, Notifires is the solution developed for our use case project, which allows the user on the one hand to insert routes and on the other hand to see whether or not the inserted routes is safe. This can be seen through the information of forest fires and how big they are.

## Getting Started

Step 1: The user has to download the [map of british columbia](https://drive.google.com/drive/folders/0BxNP1JcmjZdSZVpjZzJrN1RabHM?usp=sharing) and insert it into your eclipse workspace folder (src\main\resources).

Step 2: To try the demo, the user has to run the class _BackEnd_ and then afterwards open the Website including the map by typing localhost:4567 in the Browser. Looking at the webpage, the user may then click two points on the map (the selected points has to be streets on the map) in order to see if there is fire danger on the route.

## Prerequisites

* Java 8 or higher 
* Maven 3 or higher (http://maven.apache.org/)

## Test the project

As already mentioned before, the user can test the project by inserting routes. The user should select different routes (long and short) in different areas (close to forest and in cities) to ultimately see the difference and the fire notifications.

## Major Classes and Functions

The core functions for routing are in _BackEnd_ class, _GraphhopperHelper_ class and the _index_ html file. _BackEnd_ includes two main functions that are used for the Grid Calculation. Firstly, `distanceInKm` calculates the distance between the two coordinates (by their Longitude and Latitude) which are needed to separate the map into commensurate cluster. Secondly, with `calculateCluster` the output of `distanceInKm` (horizontal maximum length) is used to divide the map into their clusters. Further `calculateCluster` sorts the fires into the right cluster.

A JavaPairRDD uses `calculateCluster` to sort the coordinates into a cluster and map it to the hectare of the forest fire to know how big the fire is. By defining ClusterID's the fires within the same cluster may then be reduced together as one single cluster output to simply the fire detection algorithm. 

The _post_ method is used to get the start and destination coordinates from the web browser in a json. The json gets converted into a gson to process it in java. In the post method `bestPath` is called (which is in the _GarphopperHelper_ class) to find out which coordinates of the route is in a danger / fire Cluster. Finally, the gson gets converted to a json which then gets sent back to the web browser to display the results. 

**Define Cluster:**
![](http://i.imgur.com/O5t1z9Z.jpg)

The _GraphhopperHelper_ gets the start and endpoints and calculates to find the fastest way points.

The _index_ html contains the map with some additional information of start points, destination points, method and click counter to track what the user had inputted. The map is used to define the the start and destination points by clicking on it and gets the route and fire spots back.

## Example .CSV

FIRE_ID,LATITUDE,LONGITUDE,IGN_DATE,SIZE_HA,OBJECTID
122,49.1470,-121.3056,20170521000000,0.300,1                   
195,49.8141,-121.4566,20170530000000,0.009,2                    
368,49.3832,-121.3164,20170624000000,0.009,3

Relevant Columns for evaluation are: LATITUDE,LONGITUDE and SIZE_HA

## Architecture

![](http://i.imgur.com/cElHbNX.jpg)

## Video Presentation

[NotiFires Video Presentation](https://www.youtube.com/embed/a0haUpbYm18)

### Authors

* Brede, Niklas
* Emde, Markus
* Hofer, Lukas
* Neuberger, Oliver
* Stegmann, Jannis
* Wang, Eric

### License

Copyright [2017] [Niklas Brede, Markus Emde, Lukas Hofer, Oliver Neuberger, Jannis Stegmann, Eric Wang]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


