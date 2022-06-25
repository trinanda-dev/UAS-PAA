import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;
import javax.swing.*;
import javax.swing.event.*;



public class Maze50 {

    public static JFrame mazeFrame;  // The main form of the program
    public static void main(String[] args) {
        int width  = 693;
        int height = 545;
        mazeFrame = new JFrame("Smart Courier");
        mazeFrame.setContentPane(new MazePanel(width,height));
        mazeFrame.pack();
        mazeFrame.setResizable(false);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double ScreenHeight = screenSize.getHeight();
        int x = ((int)screenWidth-width)/2;
        int y = ((int)ScreenHeight-height)/2;

        mazeFrame.setLocation(x,y);
        mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mazeFrame.setVisible(true);
    } // end main()
    
    public static class MazePanel extends JPanel {
        
        private class Cell {
            int row;   // the row number of the cell(row 0 is the top)
            int col;   // the column number of the cell (Column 0 is the left)
            int g;     // the value of the function g of A* and Greedy algorithms
            int h;     // the value of the function h of A* and Greedy algorithms
            int f;     // the value of the function h of A* and Greedy algorithms
            int dist;  // the distance of the cell from the initial position of the Courier
                       // Ie the label that updates the Dijkstra's algorithm
            Cell prev; // Each state corresponds to a cell
                       // and each state has a predecessor which
                       // is stored in this variable
            
            public Cell(int row, int col){
               this.row = row;
               this.col = col;
            }
        } // end nested class Cell
      
        private class CellComparatorByF implements Comparator<Cell>{
            @Override
            public int compare(Cell cell1, Cell cell2){
                return cell1.f-cell2.f;
            }
        } // end nested class CellComparatorByF
      
        
        private class CellComparatorByDist implements Comparator<Cell>{
            @Override
            public int compare(Cell cell1, Cell cell2){
                return cell1.dist-cell2.dist;
            }
        } // end nested class CellComparatorByDist
        
        
        private class ActionHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String cmd = evt.getActionCommand();
                if (cmd.equals("Clear")) {
                    fillGrid();
                    realTime = false;
                    realTimeButton.setEnabled(true);
                    realTimeButton.setForeground(Color.black);
                    stepButton.setEnabled(true);
                    animationButton.setEnabled(true);
                    slider.setEnabled(true);
                    dfs.setEnabled(true);
                    bfs.setEnabled(true);
                    aStar.setEnabled(true);
                    greedy.setEnabled(true);
                    dijkstra.setEnabled(true);
                    diagonal.setEnabled(true);
                    drawArrows.setEnabled(true);
                } else if (cmd.equals("Real-Time") && !realTime) {
                    realTime = true;
                    searching = true;
                    realTimeButton.setForeground(Color.red);
                    stepButton.setEnabled(false);
                    animationButton.setEnabled(false);
                    slider.setEnabled(false);
                    dfs.setEnabled(false);
                    bfs.setEnabled(false);
                    aStar.setEnabled(false);
                    greedy.setEnabled(false);
                    dijkstra.setEnabled(false);
                    timer.setDelay(0);
                    timer.start();
                    if (dijkstra.isSelected()) { //
                       initializeDijkstra();
                    }
                    checkTermination();
                } else if (cmd.equals("Step-by-Step") && !found && !endOfSearch) {
                    realTime = false;
                    // The Dijkstra's initialization should be done just before the
                    // start of search, because obstacles must be in place.
                    if (!searching && dijkstra.isSelected()) {
                        initializeDijkstra();
                    }
                    searching = true;
                    realTimeButton.setEnabled(false);
                    dfs.setEnabled(false);
                    bfs.setEnabled(false);
                    aStar.setEnabled(false);
                    greedy.setEnabled(false);
                    dijkstra.setEnabled(false);
                    timer.stop();
                    checkTermination();
                    repaint();
                } else if (cmd.equals("Animation") && !endOfSearch) {
                    realTime = false;
                    if (!searching && dijkstra.isSelected()) {
                        initializeDijkstra();
                    }
                    searching = true;
                    realTimeButton.setEnabled(false);
                    dfs.setEnabled(false);
                    bfs.setEnabled(false);
                    aStar.setEnabled(false);
                    greedy.setEnabled(false);
                    dijkstra.setEnabled(false);
                    timer.setDelay(delay);
                    timer.start();
                } else if (cmd.equals("About Maze")) {
                    AboutBox aboutBox = new AboutBox(mazeFrame,false);
                    aboutBox.setVisible(true);
                }
            }
        } // end nested class ActionHandler
   
        private class RepaintAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent evt) {
                checkTermination();
                if (found) {
                    timer.stop();
                }
                if (!realTime) {
                    repaint();
                }
            }
        } // end nested class RepaintAction
      
        public void checkTermination() {
            if ((dijkstra.isSelected() && graph.isEmpty()) ||
                          (!dijkstra.isSelected() && openSet.isEmpty()) ) {
                endOfSearch = true;
                grid[CourierStart.row][CourierStart.col]=Courier;
                stepButton.setEnabled(false);
                animationButton.setEnabled(false);
                slider.setEnabled(false);
                repaint();
            } else {
                expandNode();
                if (found) {
                    endOfSearch = true;
                    plotRoute();
                    stepButton.setEnabled(false);
                    animationButton.setEnabled(false);
                    slider.setEnabled(false);
                    repaint();
                }
            }
        }


        private class AboutBox extends JDialog{

            public AboutBox(Frame parent, boolean modal){
                super(parent, modal);
                // the aboutBox is located in the center of the screen
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                double screenWidth = screenSize.getWidth();
                double ScreenHeight = screenSize.getHeight();
                int width = 350;
                int height = 190;
                int x = ((int)screenWidth-width)/2;
                int y = ((int)ScreenHeight-height)/2;
                setSize(width,height);
                setLocation(x, y);
         
                setResizable(false);
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                JLabel title = new JLabel("Smart Courier by Trinanda", JLabel.CENTER);
                title.setFont(new Font("Helvetica",Font.PLAIN,24));
                title.setForeground(new java.awt.Color(0, 0, 0));

                add(title);
                title.setBounds(5,  0, 330, 30);


            }
 
            
            
        } // end nested class AboutBox

        
        private class MyMaze {
            private int dimensionX, dimensionY; // dimension of maze
            private int gridDimensionX, gridDimensionY; // dimension of output grid
            private char[][] mazeGrid; // output grid
            private Cell[][] cells; // 2d array of Cells
            private Random random = new Random(); // The random object

            
            public MyMaze(int xDimension, int yDimension) {
                dimensionX = xDimension;
                dimensionY = yDimension;
                gridDimensionX = xDimension * 2 + 1;
                gridDimensionY = yDimension * 2 + 1;
                mazeGrid = new char[gridDimensionX][gridDimensionY];
                init();
                generateMaze();
            }

            private void init() {
                // create cells
                cells = new Cell[dimensionX][dimensionY];
                for (int x = 0; x < dimensionX; x++) {
                    for (int y = 0; y < dimensionY; y++) {
                        cells[x][y] = new Cell(x, y, false); // create cell (see Cell constructor)
                    }
                }
            }

            private class Cell {
                int x, y; // coordinates
                // cells this cell is connected to
                ArrayList<Cell> neighbors = new ArrayList<>();
                // impassable cell
                boolean wall = true;
                // if true, has yet to be used in generation
                boolean open = true;
                // construct Cell at x, y
                Cell(int x, int y) {
                    this(x, y, true);
                }
                // construct Cell at x, y and with whether it isWall
                Cell(int x, int y, boolean isWall) {
                    this.x = x;
                    this.y = y;
                    this.wall = isWall;
                }
                // add a neighbor to this cell, and this cell as a neighbor to the other
                void addNeighbor(Cell other) {
                    if (!this.neighbors.contains(other)) { // avoid duplicates
                        this.neighbors.add(other);
                    }
                    if (!other.neighbors.contains(this)) { // avoid duplicates
                        other.neighbors.add(this);
                    }
                }
                // used in updateGrid()
                boolean isCellBelowNeighbor() {
                    return this.neighbors.contains(new Cell(this.x, this.y + 1));
                }
                // used in updateGrid()
                boolean isCellRightNeighbor() {
                    return this.neighbors.contains(new Cell(this.x + 1, this.y));
                }
                // useful Cell equivalence
                @Override
                public boolean equals(Object other) {
                    if (!(other instanceof Cell)) return false;
                    Cell otherCell = (Cell) other;
                    return (this.x == otherCell.x && this.y == otherCell.y);
                }

                // should be overridden with equals
                @Override
                public int hashCode() {
                    // random hash code method designed to be usually unique
                    return this.x + this.y * 256;
                }

            }
            // generate from upper left (In computing the y increases down often)
            private void generateMaze() {
                generateMaze(0, 0);
            }
            // generate the maze from coordinates x, y
            private void generateMaze(int x, int y) {
                generateMaze(getCell(x, y)); // generate from Cell
            }
            private void generateMaze(Cell startAt) {
                // don't generate from cell not there
                if (startAt == null) return;
                startAt.open = false; // indicate cell closed for generation
                ArrayList<Cell> cellsList = new ArrayList<>();
                cellsList.add(startAt);

                while (!cellsList.isEmpty()) {
                    Cell cell;
                    if (random.nextInt(10)==0)
                        cell = cellsList.remove(random.nextInt(cellsList.size()));
                    else cell = cellsList.remove(cellsList.size() - 1);
                   
                    ArrayList<Cell> neighbors = new ArrayList<>();
                    Cell[] potentialNeighbors = new Cell[]{
                        getCell(cell.x + 1, cell.y),
                        getCell(cell.x, cell.y + 1),
                        getCell(cell.x - 1, cell.y),
                        getCell(cell.x, cell.y - 1)
                    };
                    for (Cell other : potentialNeighbors) {
                        if (other==null || other.wall || !other.open) continue;
                        neighbors.add(other);
                    }
                    if (neighbors.isEmpty()) continue;
                    Cell selected = neighbors.get(random.nextInt(neighbors.size()));
                    selected.open = false; 
                    cell.addNeighbor(selected);
                    cellsList.add(cell);
                    cellsList.add(selected);
                }
                updateGrid();
            }
            
            public Cell getCell(int x, int y) {
                try {
                    return cells[x][y];
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }
            // draw the maze
            public void updateGrid() {
                char backChar = ' ', wallChar = 'X', cellChar = ' ';
                // fill background
                for (int x = 0; x < gridDimensionX; x ++) {
                    for (int y = 0; y < gridDimensionY; y ++) {
                        mazeGrid[x][y] = backChar;
                    }
                }
                // build walls
                for (int x = 0; x < gridDimensionX; x ++) {
                    for (int y = 0; y < gridDimensionY; y ++) {
                        if (x % 2 == 0 || y % 2 == 0)
                            mazeGrid[x][y] = wallChar;
                    }
                }
                // make meaningful representation
                for (int x = 0; x < dimensionX; x++) {
                    for (int y = 0; y < dimensionY; y++) {
                        Cell current = getCell(x, y);
                        int gridX = x * 2 + 1, gridY = y * 2 + 1;
                        mazeGrid[gridX][gridY] = cellChar;
                        if (current.isCellBelowNeighbor()) {
                            mazeGrid[gridX][gridY + 1] = cellChar;
                        }
                        if (current.isCellRightNeighbor()) {
                            mazeGrid[gridX + 1][gridY] = cellChar;
                        }
                    }
                }
                
                searching = false;
                endOfSearch = false;
                fillGrid();
                for (int x = 0; x < gridDimensionX; x++) {
                    for (int y = 0; y < gridDimensionY; y++) {
                        if (mazeGrid[x][y] == wallChar && grid[x][y] != Courier && grid[x][y] != TARGET){
                            grid[x][y] = OBST;
                        }
                    }
                }
            }
        } // end nested class MyMaze
        
        private final static int
            INFINITY = Integer.MAX_VALUE, // The representation of the infinite
            EMPTY    = 0,  // empty cell
            OBST     = 1,  // cell with obstacle
            Courier    = 2,  // the position of the Courier
            TARGET   = 3,  // the position of the target
            FRONTIER = 4,  // cells that form the frontier (OPEN SET)
            CLOSED   = 5,  // cells that form the CLOSED SET
            ROUTE    = 6;  // cells that form the Courier-to-target path
        
        // Messages to the user
        
        JSpinner rowsSpinner, columnsSpinner; // Spinners for entering # of rows and columns
        
        int rows    = 20,           // the number of rows of the grid
            columns = 20,           // the number of columns of the grid
            squareSize = 500/rows;  // the cell size in pixels
        

        int arrowSize = squareSize/2; 
        ArrayList<Cell> openSet   = new ArrayList();// the OPEN SET
        ArrayList<Cell> closedSet = new ArrayList();// the CLOSED SET
        ArrayList<Cell> graph     = new ArrayList();// the set of vertices of the graph
                                                    // to be explored by Dijkstra's algorithm
         
        Cell CourierStart; // the initial position of the Courier
        Cell targetPos;  // the position of the target
              
        // basic buttons
        JButton resetButton, mazeButton, clearButton, realTimeButton, stepButton, animationButton;
        
        // buttons for selecting the algorithm
        JRadioButton dfs, bfs, aStar, greedy, dijkstra;
        
        // the slider for adjusting the speed of the animation
        JSlider slider;
        
        // Diagonal movements allowed?
        JCheckBox diagonal;
        // Draw arrows to predecessors
        JCheckBox drawArrows;

        int[][] grid;        // the grid
        boolean realTime;    // Solution is displayed instantly
        boolean found;       // flag that the goal was found
        boolean searching;   // flag that the search is in progress
        boolean endOfSearch; // flag that the search came to an end
        int delay;           // time delay of animation (in msec)
        int expanded;        // the number of nodes that have been expanded
        
        // the object that controls the animation
        RepaintAction action = new RepaintAction();
        
        // the Timer which governs the execution speed of the animation
        Timer timer;
      
        /**
         * The creator of the panel
         * @param width  the width of the panel.
         * @param height the height of the panel.
         */
        public MazePanel(int width, int height) {
      
            setLayout(null);
            
            setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.blue));

            setPreferredSize( new Dimension(width,height) );

            grid = new int[rows][columns];

            // We create the contents of the panel

           

            JLabel rowsLbl = new JLabel("# of rows (5-83):", JLabel.RIGHT);
            rowsLbl.setFont(new Font("Helvetica",Font.PLAIN,13));

            SpinnerModel rowModel = new SpinnerNumberModel(20, //initial value
                                       5,  //min
                                       83, //max
                                       1); //step
            rowsSpinner = new JSpinner(rowModel);
 
            JLabel columnsLbl = new JLabel("# of columns (5-83):", JLabel.RIGHT);
            columnsLbl.setFont(new Font("Helvetica",Font.PLAIN,13));

            SpinnerModel colModel = new SpinnerNumberModel(20, //initial value
                                       5,  //min
                                       83, //max
                                       1); //step
            columnsSpinner = new JSpinner(colModel);

            resetButton = new JButton("New grid");
            resetButton.addActionListener(new ActionHandler());
            resetButton.setBackground(Color.lightGray);
            resetButton.setToolTipText
                    ("Clears and redraws the grid according to the given rows and columns");
            resetButton.addActionListener(this::resetButtonActionPerformed);

            mazeButton = new JButton("Maze");
            mazeButton.addActionListener(new ActionHandler());
            mazeButton.setBackground(Color.lightGray);
            mazeButton.setToolTipText
                    ("Creates a random maze");
            mazeButton.addActionListener(this::mazeButtonActionPerformed);

            clearButton = new JButton("Clear");
            clearButton.addActionListener(new ActionHandler());
            clearButton.setBackground(Color.lightGray);
            clearButton.setToolTipText
                    ("First click: clears search, Second click: clears obstacles");

            realTimeButton = new JButton("Real-Time");
            realTimeButton.addActionListener(new ActionHandler());
            realTimeButton.setBackground(Color.lightGray);
            realTimeButton.setToolTipText
                    ("Position of obstacles, Courier and target can be changed when search is underway");

            stepButton = new JButton("Step-by-Step");
            stepButton.addActionListener(new ActionHandler());
            stepButton.setBackground(Color.lightGray);
            stepButton.setToolTipText
                    ("The search is performed step-by-step for every click");

            animationButton = new JButton("Animation");
            animationButton.addActionListener(new ActionHandler());
            animationButton.setBackground(Color.lightGray);
            animationButton.setToolTipText
                    ("The search is performed automatically");

            JLabel velocity = new JLabel("Speed", JLabel.CENTER);
            velocity.setFont(new Font("Helvetica",Font.PLAIN,10));
            
            slider = new JSlider(0,1000,500); // initial value of delay 500 msec
            slider.setToolTipText
                    ("Regulates the delay for each step (0 to 1 sec)");
            
            delay = 1000-slider.getValue();
            slider.addChangeListener((ChangeEvent evt) -> {
                JSlider source = (JSlider)evt.getSource();
                if (!source.getValueIsAdjusting()) {
                    delay = 1000-source.getValue();
                }
            });
            
            // ButtonGroup that synchronizes the five RadioButtons
            // choosing the algorithm, so that only one
            // can be selected anytime
            ButtonGroup algoGroup = new ButtonGroup();

            dfs = new JRadioButton("DFS");
            dfs.setToolTipText("Depth First Search algorithm");
            algoGroup.add(dfs);
            dfs.addActionListener(new ActionHandler());

            bfs = new JRadioButton("BFS");
            bfs.setToolTipText("Breadth First Search algorithm");
            algoGroup.add(bfs);
            bfs.addActionListener(new ActionHandler());

            aStar = new JRadioButton("A*");
            aStar.setToolTipText("A* algorithm");
            algoGroup.add(aStar);
            aStar.addActionListener(new ActionHandler());

            greedy = new JRadioButton("Greedy");
            greedy.setToolTipText("Greedy search algorithm");
            algoGroup.add(greedy);
            greedy.addActionListener(new ActionHandler());

            dijkstra = new JRadioButton("Dijkstra");
            dijkstra.setToolTipText("Dijkstra's algorithm");
            algoGroup.add(dijkstra);
            dijkstra.addActionListener(new ActionHandler());

            JPanel algoPanel = new JPanel();
            algoPanel.setBorder(javax.swing.BorderFactory.
                    createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
                    "Algorithms", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.TOP, new java.awt.Font("Helvetica", 0, 14)));
            
            dfs.setSelected(true);  // DFS is initially selected 
            
           diagonal = new
                    JCheckBox("Diagonal movements");
            diagonal.setToolTipText("Diagonal movements are also allowed");

           drawArrows = new
                    JCheckBox("Arrows to predecessors");
            drawArrows.setToolTipText("Draw arrows to predecessors");

            JLabel Courier = new JLabel("Courier", JLabel.CENTER);
            Courier.setForeground(Color.red);
            Courier.setFont(new Font("Helvetica",Font.PLAIN,14));

            JLabel target = new JLabel("Target", JLabel.CENTER);
            target.setForeground(Color.GREEN);
            target.setFont(new Font("Helvetica",Font.PLAIN,14));
         
            JLabel frontier = new JLabel("Frontier", JLabel.CENTER);
            frontier.setForeground(Color.blue);
            frontier.setFont(new Font("Helvetica",Font.PLAIN,14));

            JLabel closed = new JLabel("Closed set", JLabel.CENTER);
            closed.setForeground(Color.CYAN);
            closed.setFont(new Font("Helvetica",Font.PLAIN,14));


            // we add the contents of the panel
            add(rowsLbl);
            add(rowsSpinner);
            add(columnsLbl);
            add(columnsSpinner);
            add(resetButton);
            add(mazeButton);
            add(clearButton);
            add(realTimeButton);
            add(stepButton);
            add(animationButton);
            add(velocity);
            add(slider);
            //add(dfs);
            //add(bfs);
            add(aStar);
            //add(greedy);
            //add(dijkstra);
            add(algoPanel);
            add(Courier);
            add(target);
            add(frontier);
            add(closed);

            // we regulate the sizes and positions
            rowsLbl.setBounds(520, 5, 130, 25);
            rowsSpinner.setBounds(655, 5, 35, 25);
            columnsLbl.setBounds(520, 35, 130, 25);
            columnsSpinner.setBounds(655, 35, 35, 25);
            resetButton.setBounds(520, 65, 170, 25);
            mazeButton.setBounds(520, 95, 170, 25);
            clearButton.setBounds(520, 125, 170, 25);
            realTimeButton.setBounds(520, 155, 170, 25);
            stepButton.setBounds(520, 185, 170, 25);
            animationButton.setBounds(520, 215, 170, 25);
            velocity.setBounds(520, 245, 170, 10);
            slider.setBounds(520, 255, 170, 25);
            dfs.setBounds(530, 300, 70, 25);
            bfs.setBounds(600, 300, 70, 25);
            aStar.setBounds(530, 325, 70, 25);
            greedy.setBounds(600, 325, 85, 25);
            dijkstra.setBounds(530, 350, 85, 25);
            algoPanel.setLocation(520,280);
            algoPanel.setSize(170, 100);
            diagonal.setBounds(520, 385, 170, 25);
            drawArrows.setBounds(520, 200, 170, 25);
            Courier.setBounds(520, 465, 80, 25);
            target.setBounds(605, 465, 80, 25);
            frontier.setBounds(520, 485, 80, 25);
            closed.setBounds(605, 485, 80, 25);

            // we create the timer
            timer = new Timer(delay, action);
            
            // We attach to cells in the grid initial values.
            // Here is the first step of the algorithms
            fillGrid();

        } // end constructor

    static protected JSpinner addLabeledSpinner(Container c,
                                                String label,
                                                SpinnerModel model) {
        JLabel l = new JLabel(label);
        c.add(l);
 
        JSpinner spinner = new JSpinner(model);
        l.setLabelFor(spinner);
        c.add(spinner);
 
        return spinner;
    }

        /**
         * Function executed if the user presses the button "New Grid"
         */
        private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
            realTime = false;
            realTimeButton.setEnabled(true);
            realTimeButton.setForeground(Color.black);
            stepButton.setEnabled(true);
            animationButton.setEnabled(true);
            slider.setEnabled(true);
            initializeGrid(false);
        } // end resetButtonActionPerformed()
    
        /**
         * Function executed if the user presses the button "Maze"
         */
        private void mazeButtonActionPerformed(java.awt.event.ActionEvent evt) {
            realTime = false;
            realTimeButton.setEnabled(true);
            realTimeButton.setForeground(Color.black);
            stepButton.setEnabled(true);
            animationButton.setEnabled(true);
            slider.setEnabled(true);
            initializeGrid(true);
        } // end mazeButtonActionPerformed()
    
        /**
         * Creates a new clean grid or a new maze
         */
        private void initializeGrid(Boolean makeMaze) {                                           
            rows    = (int)(rowsSpinner.getValue());
            columns = (int)(columnsSpinner.getValue());
            squareSize = 500/(rows > columns ? rows : columns);
            arrowSize = squareSize/2;
            // the maze must have an odd number of rows and columns
            if (makeMaze && rows % 2 == 0) {
                rows -= 1;
            }
            if (makeMaze && columns % 2 == 0) {
                columns -= 1;
            }
            grid = new int[rows][columns];
            CourierStart = new Cell(rows-2,1);
            targetPos = new Cell(1,columns-2);
            dfs.setEnabled(true);
            dfs.setSelected(true);
            bfs.setEnabled(true);
            aStar.setEnabled(true);
            greedy.setEnabled(true);
            dijkstra.setEnabled(true);
            diagonal.setSelected(false);
            diagonal.setEnabled(true);
            drawArrows.setSelected(false);
            drawArrows.setEnabled(true);
            slider.setValue(500);
            if (makeMaze) {
                MyMaze maze = new MyMaze(rows/2,columns/2);
            } else {
                fillGrid();
            }
        } // end initializeGrid()
   
        /**
         * Expands a node and creates his successors
         */
        private void expandNode(){
            // Dijkstra's algorithm to handle separately
            if (dijkstra.isSelected()){
                Cell u;
                // 11: while Q is not empty:
                if (graph.isEmpty()){
                    return;
                }
                // 12:  u := vertex in Q (graph) with smallest distance in dist[] ;
                // 13:  remove u from Q (graph);
                u = graph.remove(0);
                // Add vertex u in closed set
                closedSet.add(u);
                // If target has been found ...
                if (u.row == targetPos.row && u.col == targetPos.col){
                    found = true;
                    return;
                }
                // Counts nodes that have expanded.
                expanded++;
                // Update the color of the cell
                grid[u.row][u.col] = CLOSED;
                // 14: if dist[u] = infinity:
                if (u.dist == INFINITY){
                    // ... then there is no solution.
                    // 15: break;
                    return;
                // 16: end if
                } 
                // Create the neighbors of u
                ArrayList<Cell> neighbors = createSuccesors(u, false);
                // 18: for each neighbor v of u:
                neighbors.stream().forEach((v) -> {
                    // 20: alt := dist[u] + dist_between(u, v) ;
                    int alt = u.dist + distBetween(u,v);
                    // 21: if alt < dist[v]:
                    if (alt < v.dist) {
                        // 22: dist[v] := alt ;
                        v.dist = alt;
                        // 23: previous[v] := u ;
                        v.prev = u;
                        // Update the color of the cell
                        grid[v.row][v.col] = FRONTIER;
                        // 24: decrease-key v in Q;
                        // (sort list of nodes with respect to dist)
                        Collections.sort(graph, new CellComparatorByDist());
                    }
                }); // The handling of the other four algorithms
            } else {
                Cell current;
                if (dfs.isSelected() || bfs.isSelected()) {
                    current = openSet.remove(0);
                } else {
                    Collections.sort(openSet, new CellComparatorByF());
                    current = openSet.remove(0);
                }
                // ... and add it to CLOSED SET.
                closedSet.add(0,current);
                // Update the color of the cell
                grid[current.row][current.col] = CLOSED;
                // If the selected node is the target ...
                if (current.row == targetPos.row && current.col == targetPos.col) {
                    // ... then terminate etc
                    Cell last = targetPos;
                    last.prev = current.prev;
                    closedSet.add(last);
                    found = true;
                    return;
                }
                expanded++;
                ArrayList<Cell> succesors;
                succesors = createSuccesors(current, false);
                // Here is the 5th step of the algorithms
                // 5. For each successor of Si, ...
                succesors.stream().forEach((cell) -> {
                    // ... if we are running DFS ...
                    if (dfs.isSelected()) {
                        // ... add the successor at the beginning of the list OPEN SET
                        openSet.add(0, cell);
                        // Update the color of the cell
                        grid[cell.row][cell.col] = FRONTIER;
                        // ... if we are runnig BFS ...
                    } else if (bfs.isSelected()){
                        // ... add the successor at the end of the list OPEN SET
                        openSet.add(cell);
                        // Update the color of the cell
                        grid[cell.row][cell.col] = FRONTIER;
                        // ... if we are running A* or Greedy algorithms (step 5 of A* algorithm) ...
                    } else if (aStar.isSelected() || greedy.isSelected()){
                        int dxg = current.col-cell.col;
                        int dyg = current.row-cell.row;
                        int dxh = targetPos.col-cell.col;
                        int dyh = targetPos.row-cell.row;
                        if (diagonal.isSelected()){
                            if (greedy.isSelected()) {
                                cell.g = 0;
                            } else {
                                cell.g = current.g+(int)((double)1000*Math.sqrt(dxg*dxg + dyg*dyg));
                            }
                            cell.h = (int)((double)1000*Math.sqrt(dxh*dxh + dyh*dyh));
                        } else {
                            if (greedy.isSelected()) {
                                cell.g = 0;
                            } else {
                                cell.g = current.g+Math.abs(dxg)+Math.abs(dyg);
                            }
                            cell.h = Math.abs(dxh)+Math.abs(dyh);
                        }
                        cell.f = cell.g+cell.h;
                        int openIndex   = isInList(openSet,cell);
                        int closedIndex = isInList(closedSet,cell);
                        if (openIndex == -1 && closedIndex == -1) {
                            openSet.add(cell);
                            grid[cell.row][cell.col] = FRONTIER;
                        } else {
                            if (openIndex > -1){
                                if (openSet.get(openIndex).f <= cell.f) {
                                } else {
                                    openSet.remove(openIndex);
                                    openSet.add(cell);
                                    grid[cell.row][cell.col] = FRONTIER;
                                }
                            } else {
                                if (closedSet.get(closedIndex).f <= cell.f) {
                                } else {
                                    closedSet.remove(closedIndex);
                                    openSet.add(cell);
                                    grid[cell.row][cell.col] = FRONTIER;
                                }
                            }
                        }
                    }
                });
            }
        } //end expandNode()
        
        private ArrayList<Cell> createSuccesors(Cell current, boolean makeConnected){
            int r = current.row;
            int c = current.col;
            ArrayList<Cell> temp = new ArrayList<>();
            if (r > 0 && grid[r-1][c] != OBST &&
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                          isInList(openSet,new Cell(r-1,c)) == -1 &&
                          isInList(closedSet,new Cell(r-1,c)) == -1)) {
                Cell cell = new Cell(r-1,c);
                if (dijkstra.isSelected()){
                    if (makeConnected) {
                        temp.add(cell);
                    } else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1) {
                            temp.add(graph.get(graphIndex));
                        }
                    }
                } else {
                    cell.prev = current;
                    temp.add(cell);
                 }
            }
            if (diagonal.isSelected()){
                if (r > 0 && c < columns-1 && grid[r-1][c+1] != OBST &&
                        (grid[r-1][c] != OBST || grid[r][c+1] != OBST) &&
                        ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                              isInList(openSet,new Cell(r-1,c+1)) == -1 &&
                              isInList(closedSet,new Cell(r-1,c+1)) == -1)) {
                    Cell cell = new Cell(r-1,c+1);
                    if (dijkstra.isSelected()){
                        if (makeConnected) {
                            temp.add(cell);
                        } else {
                            int graphIndex = isInList(graph,cell);
                            if (graphIndex > -1) {
                                temp.add(graph.get(graphIndex));
                            }
                        }
                    } else {
                        cell.prev = current;
                        temp.add(cell);
                    }
                }
            }
            if (c < columns-1 && grid[r][c+1] != OBST &&
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected())? true :
                          isInList(openSet,new Cell(r,c+1)) == -1 &&
                          isInList(closedSet,new Cell(r,c+1)) == -1)) {
                Cell cell = new Cell(r,c+1);
                if (dijkstra.isSelected()){
                    if (makeConnected) {
                        temp.add(cell);
                    } else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1) {
                            temp.add(graph.get(graphIndex));
                        }
                    }
                } else {
                    cell.prev = current;
                    temp.add(cell);
                }
            }
            if (diagonal.isSelected()){
                if (r < rows-1 && c < columns-1 && grid[r+1][c+1] != OBST &&
                        (grid[r+1][c] != OBST || grid[r][c+1] != OBST) &&
                        ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                              isInList(openSet,new Cell(r+1,c+1)) == -1 &&
                              isInList(closedSet,new Cell(r+1,c+1)) == -1)) {
                    Cell cell = new Cell(r+1,c+1);
                    if (dijkstra.isSelected()){
                        if (makeConnected) {
                            temp.add(cell);
                        } else {
                            int graphIndex = isInList(graph,cell);
                            if (graphIndex > -1) {
                                temp.add(graph.get(graphIndex));
                            }
                        }
                    } else {
                        cell.prev = current;
                        temp.add(cell);
                    }
                }
            }
            if (r < rows-1 && grid[r+1][c] != OBST &&
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                          isInList(openSet,new Cell(r+1,c)) == -1 &&
                          isInList(closedSet,new Cell(r+1,c)) == -1)) {
                Cell cell = new Cell(r+1,c);
                if (dijkstra.isSelected()){
                    if (makeConnected) {
                        temp.add(cell);
                    } else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1) {
                            temp.add(graph.get(graphIndex));
                        }
                    }
                } else {
                    cell.prev = current;
                    temp.add(cell);
                }
            }
            if (diagonal.isSelected()){
                if (r < rows-1 && c > 0 && grid[r+1][c-1] != OBST &&
                        (grid[r+1][c] != OBST || grid[r][c-1] != OBST) &&
                        ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                              isInList(openSet,new Cell(r+1,c-1)) == -1 &&
                              isInList(closedSet,new Cell(r+1,c-1)) == -1)) {
                    Cell cell = new Cell(r+1,c-1);
                    if (dijkstra.isSelected()){
                        if (makeConnected) {
                            temp.add(cell);
                        } else {
                            int graphIndex = isInList(graph,cell);
                            if (graphIndex > -1) {
                                temp.add(graph.get(graphIndex));
                            }
                        }
                    } else {
                        cell.prev = current;
                        temp.add(cell);
                    }
                }
            }
            if (c > 0 && grid[r][c-1] != OBST && 
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                          isInList(openSet,new Cell(r,c-1)) == -1 &&
                          isInList(closedSet,new Cell(r,c-1)) == -1)) {
                Cell cell = new Cell(r,c-1);
                if (dijkstra.isSelected()){
                    if (makeConnected) {
                        temp.add(cell);
                    } else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1) {
                            temp.add(graph.get(graphIndex));
                        }
                    }
                } else {
                    cell.prev = current;
                    temp.add(cell);
                }
            }
            if (diagonal.isSelected()){
                if (r > 0 && c > 0 && grid[r-1][c-1] != OBST &&
                        (grid[r-1][c] != OBST || grid[r][c-1] != OBST) &&
                        ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                              isInList(openSet,new Cell(r-1,c-1)) == -1 &&
                              isInList(closedSet,new Cell(r-1,c-1)) == -1)) {
                    Cell cell = new Cell(r-1,c-1);
                    if (dijkstra.isSelected()){
                        if (makeConnected) {
                            temp.add(cell);
                        } else {
                            int graphIndex = isInList(graph,cell);
                            if (graphIndex > -1) {
                                temp.add(graph.get(graphIndex));
                            }
                        }
                    } else {
                        cell.prev = current;
                        temp.add(cell);
                    }
                }
            }
            
            if (dfs.isSelected()){
                Collections.reverse(temp);
            }
            return temp;
        } // end createSuccesors()
        
        
        private int isInList(ArrayList<Cell> list, Cell current){
            int index = -1;
            for (int i = 0 ; i < list.size(); i++) {
                if (current.row == list.get(i).row && current.col == list.get(i).col) {
                    index = i;
                    break;
                }
            }
            return index;
        } // end isInList()
        
        
        private Cell findPrev(ArrayList<Cell> list, Cell current){
            int index = isInList(list, current);
            return list.get(index).prev;
        } // end findPrev()
        
       
        private int distBetween(Cell u, Cell v){
            int dist;
            int dx = u.col-v.col;
            int dy = u.row-v.row;
            if (diagonal.isSelected()){
                // with diagonal movements 
                // calculate 1000 times the Euclidean distance
                dist = (int)((double)1000*Math.sqrt(dx*dx + dy*dy));
            } else {
                // without diagonal movements
                // calculate Manhattan distances
                dist = Math.abs(dx)+Math.abs(dy);
            }
            return dist;
        } // end distBetween()
        
       
        private void plotRoute(){
            searching = false;
            endOfSearch = true;
            int steps = 0;
            double distance = 0;
            int index = isInList(closedSet,targetPos);
            Cell cur = closedSet.get(index);
            grid[cur.row][cur.col]= TARGET;
            do {
                steps++;
                if (diagonal.isSelected()) {
                    int dx = cur.col-cur.prev.col;
                    int dy = cur.row-cur.prev.row;
                    distance += Math.sqrt(dx*dx + dy*dy);
                } else { 
                    distance++;
                }
                cur = cur.prev;
                grid[cur.row][cur.col] = ROUTE;
            } while (!(cur.row == CourierStart.row && cur.col == CourierStart.col));
            grid[CourierStart.row][CourierStart.col]=Courier;
           
          
        } // end plotRoute()
        
       
        private void fillGrid() {
            if (searching || endOfSearch){ 
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        if (grid[r][c] == FRONTIER || grid[r][c] == CLOSED || grid[r][c] == ROUTE) {
                            grid[r][c] = EMPTY;
                        }
                        if (grid[r][c] == Courier){
                            CourierStart = new Cell(r,c);
                        }
                        if (grid[r][c] == TARGET){
                            targetPos = new Cell(r,c);
                        }
                    }
                }
                searching = false;
            } else {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        grid[r][c] = EMPTY;
                    }
                }
                CourierStart = new Cell(rows-2,1);
                targetPos = new Cell(1,columns-2);
            }
            if (aStar.isSelected() || greedy.isSelected()){
                CourierStart.g = 0;
                CourierStart.h = 0;
                CourierStart.f = 0;
            }
            expanded = 0;
            found = false;
            searching = false;
            endOfSearch = false;
         
            
            openSet.removeAll(openSet);
            openSet.add(CourierStart);
            closedSet.removeAll(closedSet);
         
            grid[targetPos.row][targetPos.col] = TARGET; 
            grid[CourierStart.row][CourierStart.col] = Courier;
            timer.stop();
            repaint();
            
        } // end fillGrid()

        
        private void findConnectedComponent(Cell v){
            Stack<Cell> stack;
            stack = new Stack();
            ArrayList<Cell> succesors;
            stack.push(v);
            graph.add(v);
            while(!stack.isEmpty()){
                v = stack.pop();
                succesors = createSuccesors(v, true);
                for (Cell c: succesors) {
                    if (isInList(graph, c) == -1){
                        stack.push(c);
                        graph.add(c);
                    }
                }
            }
        } // end findConnectedComponent()
        
      
        private void initializeDijkstra() {
            // First create the connected component
            // to which the initial position of the Courier belongs.
            graph.removeAll(graph);
            findConnectedComponent(CourierStart);
            // Here is the initialization of Dijkstra's algorithm 
            // 2: for each vertex v in Graph;
            for (Cell v: graph) {
                // 3: dist[v] := infinity ;
                v.dist = INFINITY;
                // 5: previous[v] := undefined ;
                v.prev = null;
            }
            // 8: dist[source] := 0;
            graph.get(isInList(graph,CourierStart)).dist = 0;          

            // Sorts the list of nodes with respect to 'dist'.
            Collections.sort(graph, new CellComparatorByDist());
            // Initializes the list of closed nodes
            closedSet.removeAll(closedSet);
        } // end initializeDijkstra()

        
        @Override
        public void paintComponent(Graphics g) {

            super.paintComponent(g);  // Fills the background color.

            g.setColor(Color.DARK_GRAY);
            g.fillRect(10, 10, columns*squareSize+1, rows*squareSize+1);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (grid[r][c] == EMPTY) {
                        g.setColor(Color.WHITE);
                    } else if (grid[r][c] == Courier) {
                        g.setColor(Color.RED);
                    } else if (grid[r][c] == TARGET) {
                        g.setColor(Color.GREEN);
                    } else if (grid[r][c] == OBST) {
                        g.setColor(Color.BLACK);
                    } else if (grid[r][c] == FRONTIER) {
                        g.setColor(Color.BLUE);
                    } else if (grid[r][c] == CLOSED) {
                        g.setColor(Color.CYAN);
                    } else if (grid[r][c] == ROUTE) {
                        g.setColor(Color.YELLOW);
                    }
                    g.fillRect(11 + c*squareSize, 11 + r*squareSize, squareSize - 1, squareSize - 1);
                }
            }
           
            
            if (drawArrows.isSelected()) {
                // We draw all arrows from each open or closed state
                // to its predecessor.
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        if ((grid[r][c] == TARGET && found)  || grid[r][c] == ROUTE  || 
                                grid[r][c] == FRONTIER || (grid[r][c] == CLOSED &&
                                !(r == CourierStart.row && c == CourierStart.col))){
                            Cell head;
                            if (grid[r][c] == FRONTIER){
                                if (dijkstra.isSelected()){
                                    head = findPrev(graph,new Cell(r,c));
                                } else {
                                    head = findPrev(openSet,new Cell(r,c));
                                }
                            } else {
                                head = findPrev(closedSet,new Cell(r,c));
                            }
                            // The coordinates of the center of the current cell
                            int tailX = 11+c*squareSize+squareSize/2;
                            int tailY = 11+r*squareSize+squareSize/2;
                            // The coordinates of the center of the predecessor cell
                            int headX = 11+head.col*squareSize+squareSize/2;
                            int headY = 11+head.row*squareSize+squareSize/2;
                            // If the current cell is the target
                            // or belongs to the path to the target ...
                            if (grid[r][c] == TARGET  || grid[r][c] == ROUTE){
                                // ... draw a red arrow directing to the target.
                                g.setColor(Color.RED);
                                drawArrow(g,tailX,tailY,headX,headY);
                            // Else ...
                            } else {
                                // ... draw a black arrow to the predecessor cell.
                                g.setColor(Color.BLACK);
                                drawArrow(g,headX,headY,tailX,tailY);
                            }
                        }
                    }
                }
            }
        } // end paintComponent()
        
        
        private void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
            Graphics2D g = (Graphics2D) g1.create();

            double dx = x2 - x1, dy = y2 - y1;
            double angle = Math.atan2(dy, dx);
            int len = (int) Math.sqrt(dx*dx + dy*dy);
            AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
            at.concatenate(AffineTransform.getRotateInstance(angle));
            g.transform(at);

            g.drawLine(0, 0, len, 0);
            g.drawLine(0, 0, (int)(arrowSize*Math.sin(70*Math.PI/180)) , (int)(arrowSize*Math.cos(70*Math.PI/180)));
            g.drawLine(0, 0, (int)(arrowSize*Math.sin(70*Math.PI/180)) , -(int)(arrowSize*Math.cos(70*Math.PI/180)));
        } // end drawArrow()
        
    } // end nested classs MazePanel
  
} // end class Maze