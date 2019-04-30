import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;
    private int clickNodeID =0;
	private int lastNodeID = 0;
	
	 private Stack<Node> aNodes= new Stack<Node>();
	    private List<Segment> aSegments = new ArrayList<Segment>();
	private double toalPathDistance = 0;
	
	//articulation point stuff
	
	   private HashSet<Node> articulationNodes = new HashSet<Node>();
	    private HashSet<Node> visitedNodeSet = new HashSet<Node>();
	
	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		if(this.clickNodeID == 0) {
		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
				this.clickNodeID = node.nodeID;//works
			}
			
			if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) { // prints first nodes infomation
				graph.setHighlight(closest);
				getTextOutputArea().setText(closest.toString());
			}
		}
		
		}else if(this.lastNodeID == 0) {
			
			for (Node node : graph.nodes.values()) {
				double distance = clicked.distance(node.location);
				if (distance < bestDist) {
					bestDist = distance;
					closest = node;
					
					this.lastNodeID = node.nodeID;
								}
				if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) { // prints second nodes infomation
					graph.setHighlight(closest);
					getTextOutputArea().setText(closest.toString());
				}
				
			}//chrashes after a third click, this will be fixed in the aStar method as it will reset after
			
			
		}
		
		System.out.println(this.clickNodeID + "	"+ this.lastNodeID);
		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
		}
		
		if(!(lastNodeID == 0)) {
			
			this.aStarSearch();
			
		}
	}

	
	//go through all the segemnts adding the ajacent nodes
	
	
	
	
	
	public void aStarSearch() {
		
		
		//reset the aStar nodes and feilds
		
		PriorityQueue<Node> fringe = new PriorityQueue<Node>();
	
		Node destinationNode = Graph.nodes.get(this.lastNodeID); // needed to change the nodes map in graph to static unsure why ask ay help desk
		Node startNode = Graph.nodes.get(this.clickNodeID);
		
		//set the values of destination node
		startNode.setPerent(null);
		startNode.setTotalCostToNode(0);
		startNode.setEstimateToNode(startNode.getLocation().distance(Graph.nodes.get(lastNodeID).getLocation()));//get the distance to the goal node
		fringe.offer(startNode); // add the starting node to the fringe
		
		
		
		while(!fringe.isEmpty()) {
			
			Node currentNode = fringe.poll(); // take the node at the front of the queue off
			Node currentFromNode = currentNode.getPerentNode(); //set CurrentFromNode to the currents node perent
			double costToNode = currentNode.getTotalCostToNode(); 
			double costToEnd = currentNode.goalEstimate();
			
			
			
			if(!currentNode.isVisited()) { //if the current node has not been visited

				currentNode.makeVisited(true); //make it vissited
				currentNode.setPerent(currentFromNode); //set its from node to the perent node of curretnNode
				//System.out.println(currentNode +"		"+ currentFromNode + "	works till here"); 
			
				currentNode.setTotalCostToNode(costToNode); // set the total cost to costToNode
				System.out.println(destinationNode.nodeID +"-------------"+currentNode.nodeID);

			
				
				if(currentNode == destinationNode) { //found the destination
					//adds nodes that make up the shotest path to the aStar feilds
					System.out.println("call 1 -----------------------------------");

					Node nextNode = destinationNode;
					
					while(nextNode != null) {
						this.aNodes.push(nextNode);
						
						nextNode = nextNode.getPerentNode();
						/**this will go back though the nodes making up the shortest path
						 * by getting there perent node till it reaches the stsrting node
						 * adding them to the stack of a star nodes
						**/ 
						
					}
					
				    getTextOutputArea().append("\n\n" + "Displaying route from NODE ID: " + startNode.getNodeID() + " to NODE ID: " + destinationNode.getNodeID() + "\n\n");
				    fringe = null;

				    this.printResults();
					//System.out.println("print method call -----------------------------------");	
					return;
					
				}
			//	System.out.println("works");

				for(Segment s: currentNode.segments) {

					
					Node neighbour = Graph.nodes.get(s.end.nodeID); //gets the node connected to the segment
					

					
					if(!neighbour.isVisited()) {
						neighbour.setPerent(currentNode);
						neighbour.setTotalCostToNode(costToNode + s.getLength());//cost to the node
						neighbour.setEstimateToNode(costToNode + s.getLength() + neighbour.getLocation().distance(destinationNode.getLocation())); 
						/** 
						 * gets the estimated cost from the neighbour node to the destination node
						 * 
						 * **/
						fringe.offer(neighbour);
						//System.out.println("works2");
					}
					
					
				}
				
			}
			
		}
		System.out.println("path not found");
		
	}
	
	public void printResults() {
		/**
		 * 
		 *  needs to go though the aStar nodes and check that that 
		 *  
		 *  **/
		
		
		
		Node firstNode = aNodes.pop();
		while(! aNodes.isEmpty()) {
			System.out.println("results 1 -----------------------------------");
			Node secondNode =  aNodes.pop();
			
			//if they share a sgment add that segemnt toasegs
			
			for(Segment s :firstNode.segments) {
				//System.out.println("results 2 -----------------------------------");
				//System.out.println(s.getEndNode().getNodeID()+ " **************");
				//System.out.println(firstNode.getNodeID()+ " *************");
				

				//if first and seconds is equal the id of the current segments end node 
			if(firstNode.getNodeID() == s.getEndNode().nodeID || secondNode.getNodeID() == s.getEndNode().nodeID) {//fix
				System.out.println("results 3 -----------------------------------");
						this.aSegments.add(s);
				
			}
			firstNode = secondNode;

			}
			/**make a new map with the name of the road and its city as the key and the length of the road as its value (city means more town not just acuckland so it makes they more identafiable)
			
			**/
			LinkedHashMap<String, Double>roads = new LinkedHashMap<String, Double>();
			
			for(int i = 0; i < this.aSegments.size(); i++) {
			String keyValue = Graph.roads.get(this.aSegments.get(i).getRoadID()).getName() + "  " + Graph.roads.get(this.aSegments.get(i).getRoadID()).getCity(); 
			//System.out.println("map for working -----------------------------------"); working

			if(roads.containsKey(keyValue)) {
				//System.out.println("map if working-----------------------------------");working

				//if the value already exists in the map just add the legth of the segment to the map value 
				roads.put(keyValue , roads.get(keyValue) + aSegments.get(i).getLength());
				
			}else {
				//else add the key to the map as a new value
				
				roads.put(keyValue, aSegments.get(i).length);
			}
		
		}
			//	go through the map and print  the roads and there lengths also the total lenth of the shortest path

		
		
		for(String key : roads.keySet()) {
			
			  getTextOutputArea().append(key +"		"+roads.get(key) +" km" + "\n\n");		
			  toalPathDistance += roads.get(key);
		}
		
			
		}
		getTextOutputArea().append("total distance of path is	" + toalPathDistance + "	km");
	}
	
	
	//********************************************
	
	public  void getarticulationPoints(){
		
		
		for(Node n : Graph.nodes.values()) {
		if(!visitedNodeSet.contains(n)) { //while there is a unvisited node
			Node startNode = n;

			for(Segment s: startNode.segments) {

				
				Node neighbour = Graph.nodes.get(s.end.nodeID); // adding the ajacent nodes to the current node
				startNode.addNextToNodesID(neighbour.getNodeID());
				
			
			}
			
			
			startNode.setDepth(0); //set the initial nodes depth to 0
			int subTrees =0;

			for(int id : startNode.getNextToNodeIDs()) {
				//System.out.println(id);

				Node nextTo = Graph.nodes.get(id);
			//	if(nextTo.getDepth() == Integer.MAX_VALUE) {
					
					
					this.articulationPoints(nextTo, 1, startNode);

			//	}
			}
			
			if(subTrees > 1) {
				
				this.articulationNodes.add(startNode);
			}
			//out put number of articulation points 
			getTextOutputArea().append("work");
		}
		getTextOutputArea().append("there are " + articulationNodes.size() +"	articualtion points");
		}
		System.out.println(("there are " + articulationNodes.size() +"	articualtion points"));
		
		
		getTextOutputArea().append("there are " + articulationNodes.size() +"	articualtion points");

	}
	
	
    public int articulationPoints(Node node, int depth, Node fromNode){
    

    	
    	//node.setReachBack(depth);
    //	System.out.println(node.getNextToNodeIDs() + " 	fffffffff") ;
    	//System.out.println(fromNode.getNextToNodeIDs() + " 	eeeeeeee") ;
    	//System.out.println(depth + "  dddddddddddd") ;

 
    	int reachBack = depth;
    	
    	
    	for(int id : node.getNextToNodeIDs()) { //null 
    		

    		
    		Node neighbour = Graph.nodes.get(id);
    		this.visitedNodeSet.add(neighbour);
    		
    		if(neighbour != fromNode) {


    			
    				if(neighbour.getDepth() < Integer.MAX_VALUE) {
    					

    					
    					reachBack = Math.min(neighbour.getDepth(), reachBack); // finds the smaller value of reachBack
    																			// and depth and sets reach back to it
    				}else {
    					
    					int childReach = articulationPoints(neighbour, depth+1, node); //recursive call 
    					if(childReach >= depth) {
    						
    						this.articulationNodes.add(node);
    						
    					}
    					reachBack = Math.min(childReach, reachBack);
    				}
    					
    				}
    		}
    		
    
    	

    	return reachBack;
    	
    
    }
    
	
	//this will return nodes for articulation points while there exists a node that is not visited
	
	
	
	
	
	
	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		if(query.equalsIgnoreCase("ap")) {
			getarticulationPoints();
			
		}
		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
		
		
		
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}
}

// code for COMP261 assignments