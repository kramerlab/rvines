<!DOCTYPE html>
<html lang="en">

<head>
<meta charset="utf-8">
<title>Visualization for Regular Vines</title>
<script src="http://d3js.org/d3.v3.min.js" charset="utf-8"></script>
<style>

#wrapper {
    width: 1200px;
    overflow: hidden; /* add this to contain floated children */
}
#menubar {
    width: 200px;
    float:left;
}
#content {
    float: right;
    width: 1000px;
    height: 500px;
}

h1 {
	text-align:center;
}

ul {
    list-style-type: none;
    margin: 0;
    padding: 0;
}

table, th, td {
    border: 1px solid black;
    border-collapse: collapse;
}

th, td {
    padding: 15px;
}

.node circle {
  cursor: pointer;
  stroke: grey;
  stroke-width: 1px;
  fill: white;
}

.node text {
  font: 10px sans-serif;
  pointer-events: none;
  text-anchor: middle;
}

line.link {
  fill: none;
  stroke-width: 1.5px;
}

</style>
</head>

<body>

<h1>Visualization for Regular Vines</h1>

<div id="wrapper">
	<div id="menubar" style="text-align:center">
		<select id="opts" onchange="request(this.value);">
			<option value="General Information">General Information</option>
		</select>
		<p></p>
		<ul id="legend">
			<li style="text-decoration: underline">Legend</li>
		</ul>
	</div>
	<div id="content" style="text-align:center">
	<h2 id = "contentTitle">General Information</h2>
	<div id="info" style="text-align":center"></div>
	</div>
</div>

<script type="text/javascript">

window.onload = function(){
	request("initialize");
}

//initialize function
function initialize(data){
	//initialize legend
	for(var i in data.legend){
		var ul = document.getElementById("legend");
		var li = document.createElement("li");
		li.appendChild(document.createTextNode(data.legend[i]));
		ul.appendChild(li);
	};
	
	//create menubar entries
	var trees = data.main["Model Information"].Trees;
	for(var i=0;i<trees;i++){
		var sel = document.getElementById("opts");
		var opt = document.createElement("option");
		opt.appendChild(document.createTextNode("T"+(i+1)));
		sel.appendChild(opt);
	}
	
	createGeneralInformation(data.main);
}

//general information function
function createGeneralInformation(data){
	var content = document.getElementById("content");
	var info = document.getElementById("info");
	info.setAttribute("style","text-align:center");
	
	//create used copulae table
	var h3_1 = document.createElement("h3");
	var text1 = document.createTextNode("Used Copulae");
	h3_1.appendChild(text1)
	info.appendChild(h3_1);
	
	var table1 = document.createElement("table");
	table1.setAttribute("align","center");
	var table1_row1 = document.createElement("tr");
	var table1_row2 = document.createElement("tr");
	
	for(var x in data["Used Copulae"]){
		var entry1 = document.createElement("td");
		var entry2 = document.createElement("td");
		
		entry1.appendChild(document.createTextNode(x));
		entry2.appendChild(document.createTextNode(data["Used Copulae"][x]));

		table1_row1.appendChild(entry1);
		table1_row2.appendChild(entry2);
	}
	table1.appendChild(table1_row1);
	table1.appendChild(table1_row2);
	info.appendChild(table1);
	
	//create model information table
	var h3_2 = document.createElement("h3");
	var text2 = document.createTextNode("Model Information");
	h3_2.appendChild(text2)
	info.appendChild(h3_2);
	
	var table2 = document.createElement("table");
	table2.setAttribute("align","center");
	var table2_row1 = document.createElement("tr");
	var table2_row2 = document.createElement("tr");
	
	for(var x in data["Model Information"]){
		var entry1 = document.createElement("td");
		var entry2 = document.createElement("td");
		
		entry1.appendChild(document.createTextNode(x));
		entry2.appendChild(document.createTextNode(data["Model Information"][x]));

		table2_row1.appendChild(entry1);
		table2_row2.appendChild(entry2);
	}
	table2.appendChild(table2_row1);
	table2.appendChild(table2_row2);
	info.appendChild(table2);
	
	//add info to content div
	content.appendChild(info);
}

//request function
function request(req) {
	console.log(req),
	d3.xhr("VineServlet")
	.header("Content-Type", "application/json")
	.header("MyRequest",req)
	.post(
	    "MyMethod",
	    function(err, rawData){
	        var data = JSON.parse(rawData.responseText);
	        console.log(data)
	        if(req == "initialize"){
	        	initialize(data);
	        } else {
	        	document.getElementById("contentTitle").innerHTML = req;
	        	d3.select("svg").remove();
			    if(req == "General Information"){
			    	createGeneralInformation(data);
			    }else{
			    	document.getElementById("info").innerHTML = "";
			    	setD3Configs();
			    	update(data);
			    }
	        }
	    }
	);
}

//d3 configuration
var width = 960,
    height = 500,
    colors = d3.scale.category10(),
    svg,
    link,
    node;

var force = d3.layout.force()
    .linkDistance(80)
    .charge(-120)
    .gravity(.05)
    .size([width, height])
    .on("tick", tick)

function setD3Configs(){
	svg = d3.select("#content").append("svg")
	    .attr("width", width)
	    .attr("height", height);
	
	link = svg.selectAll(".link"),
	node = svg.selectAll(".node");
}

function update(data) {
  var nodes = data.nodes,
  	  links = data.edges;
  
  // Restart the force layout.
  force
      .nodes(nodes)
      .links(links)
      .start();

  // Update links.
  link = link.data(links, function(d) { return d.source.id+" "+d.target.id; });

  link.exit().remove();

  link.enter().append("line", ".node")
      .attr("class", "link")
	  .attr("stroke", function(d){return colors(d.label);});

  // Update nodes.
  node = node.data(nodes, function(d) { return d.id; });

  node.exit().remove();

  var nodeEnter = node.enter().append("g")
      .attr("class", "node")
      .call(force.drag);

  nodeEnter.append("circle")
      .attr("r", function(d) { return Math.sqrt(d.size) / 10 || 10.0; });

  nodeEnter.append("text")
      .attr("dy", ".35em")
      .text(function(d) { return d.name; });
}

function tick() {
  link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
}
</script>

</body>
</html>