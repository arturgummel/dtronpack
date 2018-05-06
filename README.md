INFO
------------
Current package includes a program which is written in Java language to generate [UPPAAL models](http://uppaal.org/) from topological map.
Also it includes script to run test with [DTRON](https://cs.ttu.ee/dtron/about.html), but for that it is necessary manually install [TRON](http://people.cs.aau.dk/~marius/tron/download.html).

More information about topological map can be found [here](http://strands.readthedocs.io/en/latest/strands_navigation/wiki/Topological-Map-Definition.html).

SETUP
-----------
Topological map file must have following structure:<br/>
<pre>
node:
	#node_name
	WayPoint1
	waypoint:
	#position of the node
	position.x,position.y,position.z,orientation.x,orientation.y,orientation.z,orientation.w
	edges:
		#list of connections from this node, action
		WayPoint2, move_base
	vertices:
		#positions around the node
		position.x1,position.y1
		position.x2,position.y2
		position.x3,position.y3
		position.x4,position.y4
		position.x5,position.y5
		position.x6,position.y6
node:
    ...
</pre>

To generate the UPPAAL model type next command where *input_file.tmap* has to be replaced with your topological map file name.<br/>
`$ java -jar generateModelFromTmap.jar input_file.tmap`
