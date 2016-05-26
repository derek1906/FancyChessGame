import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * GUI Drawer. Handles drawing of the GUI.
 */
public class BoardDrawer {
    private SquareBoard gameboard;
    BoardDrawer self = this;
    JFrame window;      /**< Frame for the window*/
    JPanel panel;
    JPanel canvas;
    JPanel board;
    Grid tiles[][];
    JLabel statusText;
    ScoreBar scores;
    ScoreKeeper scoreKeeper;

    BoardDrawer(SquareBoard gameboard, String title){
        this.gameboard = gameboard;
        this.scoreKeeper = gameboard.rule.scoreKeeper;

        try{ UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e) {
            //silently ignore
        }
        System.setProperty("awt.useSystemAAFontSettings","lcd");
        System.setProperty("swing.aatext", "true");

        window = new JFrame(title);
        window.setSize(550, 700);
        window.setMinimumSize(new Dimension(550, 700));

        panel = new JPanel();               // base JPanel
        panel.setLayout(new BorderLayout());

        initMenu();         // Initialize menu bar
        initComponents();   // Initialize components

        window.setContentPane(panel);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Create JLabels for indices.
     */
    private void generateIndices(){
        GridBagConstraints constraints = new GridBagConstraints();
        for(int side = 0; side < 2; side++) {
            for (int i = 0; i < gameboard.dimension; i++) {
                //letters
                constraints.gridx = i + 1;
                constraints.gridy = side == 0 ? 0 : gameboard.dimension + 1;
                JLabel label = new JLabel(String.valueOf((char) (i + 97)), SwingConstants.CENTER);
                label.setFont(new Font("Noto Sans", Font.PLAIN, 16));
                label.setPreferredSize(new Dimension(500 / (gameboard.dimension + 2), 500 / (gameboard.dimension + 2)));
                board.add(label, constraints);

                //numbers
                constraints.gridx = side == 0 ? 0 : gameboard.dimension + 1;
                constraints.gridy = i + 1;
                label = new JLabel(String.valueOf(gameboard.dimension - i), SwingConstants.CENTER);
                label.setFont(new Font("Noto Sans", Font.PLAIN, 16));
                label.setPreferredSize(new Dimension(500 / (gameboard.dimension + 2), 500 / (gameboard.dimension + 2)));
                board.add(label, constraints);
            }
        }
    }

    /**
     * Create JLabels as grids to hold a chess symbol.
     */
    private void generateTiles(){
        tiles = new Grid[gameboard.dimension][gameboard.dimension];

        GridBagConstraints constraints = new GridBagConstraints();
        Color lightTile = new Color(255, 206, 158),
                darkTile = new Color(209, 139, 71);

        for(int y = 0; y < gameboard.dimension; y++){
            for(int x = 0; x < gameboard.dimension; x++) {
                constraints.gridx = x + 1;
                constraints.gridy = y + 1;
                Grid label = new Grid(gameboard.getLoc(x, y));
                label.setBackground((x + y) % 2 == 0 ? lightTile : darkTile);
                board.add(label, constraints);

                tiles[x][y] = label;
            }
        }
    }

    /**
     * Initialize components
     */
    private void initComponents() {
        // Header layout
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel(
                "<html>" +
                        "<div style='padding: 10px 10px; font-size: 18px; font-family: Roboto;'>" +
                            "<span style='color: #009688;'>fancy</span> <span style='color: #FF9800'>chess game.</span>" +
                        "</div>" +
                "</html>", SwingConstants.LEFT
        );
        title.setOpaque(true);
        //title.setBackground(new Color(62, 70, 76));

        scores = new ScoreBar();
        JPanel scoreBar = scores.getPanel();

        header.add(title, BorderLayout.NORTH);
        header.add(scoreBar, BorderLayout.SOUTH);

        // Container for the board
        canvas = new JPanel(new GridBagLayout());
        //canvas.setBackground(new Color(55, 64, 70));
        canvas.setBackground(new Color(219, 219, 219));

        // Board layout
        board = new JPanel(new GridBagLayout());
        board.setPreferredSize(new Dimension(500, 500));
        canvas.add(board);

        // Generate content in board
        generateIndices();
        generateTiles();

        // Container for footer
        JPanel footer = new JPanel(new GridLayout(1, 2));
        footer.setBackground(new Color(219, 219, 219));
        statusText = new JLabel("", SwingConstants.LEFT);   // Status text
        footer.add(statusText);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.setOpaque(false);

        JLabel forfeitBtn = new HoverableImageButton("white_flag.png", new Dimension(30, 30));
        forfeitBtn.setToolTipText("Forfeit");
        forfeitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int value = JOptionPane.showConfirmDialog(null, "Forfeit this game?", scoreKeeper.getPlayer(gameboard.getTurn()).name, JOptionPane.YES_NO_OPTION);
                if(value == JOptionPane.YES_OPTION){
                    gameboard.rule.gameStatus.setForfeited(gameboard.getTurn().opposite());
                    update();
                }
            }
        });
        JLabel restartBtn = new HoverableImageButton("restart.png", new Dimension(30, 30));
        restartBtn.setToolTipText("Restart");
        restartBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int value = JOptionPane.showConfirmDialog(null, "Do both players agree to restart this game?", "Confirm", JOptionPane.YES_NO_OPTION);
                if(value == JOptionPane.YES_OPTION){
                    initiateNewGame(false);
                }
            }
        });
        JLabel undoBtn = new HoverableImageButton("undo.png", new Dimension(30, 30));
        undoBtn.setToolTipText("Undo previous move");
        undoBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(gameboard.undo(1)) {
                    update();
                }else{
                    JOptionPane.showMessageDialog(null, "Cannot undo further. Are you trying to create a black hole?", "Can't undo!", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JLabel historyBtn = new HoverableImageButton("history.png", new Dimension(30, 30));
        historyBtn.setToolTipText("Show move history");
        historyBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String[] history = gameboard.getMoveHistory();
                String msg = history.length == 0 ? "Waiting for first move." : String.join(" ", history);
                JOptionPane.showMessageDialog(null, msg, "Move History", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        toolbar.add(historyBtn);
        toolbar.add(undoBtn);
        toolbar.add(forfeitBtn);
        toolbar.add(restartBtn);
        footer.add(toolbar);

        // Append elements
        panel.add(header, BorderLayout.NORTH);   // Add header
        panel.add(canvas, BorderLayout.CENTER); // Add canvas
        panel.add(footer, BorderLayout.SOUTH);  // Add footer
    }

    /**
     * Initialize menu bar.
     */
    private void initMenu(){
        JMenuBar menubar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem undo = new JMenuItem("Undo");
        undo.addActionListener(e -> {
            gameboard.undo(1);
            update();
        });
        JCheckBoxMenuItem customPieces = new JCheckBoxMenuItem("Custom Pieces");
        customPieces.addActionListener(evt -> {
            boolean isCheck = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
            if(isCheck){
                JOptionPane.showMessageDialog(null, "Custom pieces are enabled for the next game.", "Custom Pieces", JOptionPane.INFORMATION_MESSAGE);
            }
            gameboard.rule.setCustomPieces(isCheck);
        });

        gameMenu.add(undo);
        gameMenu.add(customPieces);
        menubar.add(gameMenu);
        window.setJMenuBar(menubar);
    }

    /**
     * Update the content in the GUI based on current data.
     */
    public void update(){
        // update pieces graphics
        for(int x = 0; x < gameboard.dimension; x++){
            for(int y = 0; y < gameboard.dimension; y++){
                ChessPiece piece = gameboard.grid[x][y];
                Grid grid = tiles[x][y];
                if(piece != null) {
                    grid.setText(String.valueOf(piece.getSymbol()));
                    if(piece.side == gameboard.getTurn()) {
                        grid.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }else{
                        grid.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }else{
                    grid.setText("　");
                    grid.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }

        // update game scores
        scores.setCheckSide(gameboard.sideInCheck);
        scores.update();

        // update game status
        String msg;
        switch (gameboard.rule.gameStatus.status){
            case NOT_ENDED:
                setStatusText(scoreKeeper.getPlayer(gameboard.getTurn()).name + "'s Turn");
                break;
            case CHECKMATE:
                msg = "Checkmate! " + scoreKeeper.getPlayer(gameboard.rule.gameStatus.winningSide).name + " wins!";
                setStatusText(msg);
                JOptionPane.showMessageDialog(null, msg, "Checkmate", JOptionPane.INFORMATION_MESSAGE);
                initiateNewGame(true);
                break;
            case STALEMATE:
                msg = "Stalemate! Nobody wins!";
                setStatusText(msg);
                JOptionPane.showMessageDialog(null, msg, "Stalemate", JOptionPane.INFORMATION_MESSAGE);
                initiateNewGame(true);
                break;
            case FORFEITED:
                msg = scoreKeeper.getPlayer(gameboard.rule.gameStatus.winningSide.opposite()).name + " has forfeited!";
                setStatusText(msg);
                JOptionPane.showMessageDialog(null, msg, "Boooo!", JOptionPane.INFORMATION_MESSAGE);
                initiateNewGame(true);
                break;
        }
    }

    public void initiateNewGame(boolean switchSides){
        if(switchSides) {
            // switch players
            gameboard.rule.scoreKeeper.switchSide();
        }
        // reset board
        gameboard.resetPosition();
        // update gui
        update();
    }

    /**
     * Set status below the board.
     * @param status    Message to be displayed
     */
    public void setStatusText(String status){
        statusText.setText("<html><div style='font-size: 12px; font-family: Roboto; padding: 5px 10px;'>" + status + "</div></html>");
    }

    public Grid getGrid(Location location){
        return tiles[location.x][location.y];
    }

    /**
     * Custom JLabel as grids
     */
    private class Grid extends JLabel{
        Location location;

        Grid(Location location){
            this.location = location;

            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.addMouseListener(new GridMouseEvent());
            this.setPreferredSize(new Dimension(500/(gameboard.dimension + 2), 500/(gameboard.dimension + 2)));
            this.setOpaque(true);
            this.setFont(new Font("Sans", Font.PLAIN, 500/(gameboard.dimension + 2) - 4));
        }

        /**
         * Highlight the grid.
         */
        public void highlight(){
            this.setBorder(new LineBorder(new Color(25, 118, 210), 2));
        }

        /**
         * Unhighlight the grid.
         */
        public void unhighlight(){
            this.setBorder(null);
        }

        /**
         * Hint a grid.
         */
        public void hint(){
            this.setBorder(new LineBorder(new Color(76, 175, 80), 2));
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /**
         * Unhint a grid.
         */
        public void unhint(){
            this.setBorder(null);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        /**
         * Click event for grids
         */
        private class GridMouseEvent extends MouseAdapter{
            public void mousePressed(MouseEvent e){
                Grid grid = (Grid) e.getSource();

                NormalGameRule.ConsiderResult result = gameboard.rule.handleClickEvents(grid.location);
                if(result.success) {
                    switch (result.state) {
                        case PICKING_PRIMARY:
                            grid.highlight();
                            grid.hintMovableLocations(gameboard.getPieceAtLocation(grid.location));
                            break;

                        case PICKING_SECONDARY:
                            if(result.origin == null){
                                // no origin, toggle box
                                grid.unhighlight();
                                unhighlight();
                                grid.unhintAllLocations();
                            }else if(result.switchToggle){
                                // switch toggle
                                grid.unhintAllLocations();
                                getGrid(result.origin).unhighlight();
                                grid.highlight();
                                grid.hintMovableLocations(gameboard.getPieceAtLocation(grid.location));
                            } else {
                                // origin included, reset origin border
                                getGrid(result.origin).unhighlight();
                                grid.unhintAllLocations();
                            }
                            self.update();
                            break;
                    }
                }
            }
        }

        private void hintMovableLocations(ChessPiece piece){
            for(int x = 0; x < gameboard.dimension; x++){
                for(int y = 0; y < gameboard.dimension; y++){
                    Location location = gameboard.getLoc(x, y);
                    if(gameboard.move(piece, location, true)){
                        getGrid(location).hint();
                    }
                }
            }
        }

        private void unhintAllLocations(){
            for(int x = 0; x < gameboard.dimension; x++){
                for(int y = 0; y < gameboard.dimension; y++){
                    Location location = gameboard.getLoc(x, y);
                    getGrid(location).unhint();
                }
            }
        }
    }

    /**
     * Custom JLabel with gradient background.
     */
    private class GradientLabel extends JLabel {
        Color top, bottom;

        GradientLabel(String text, int alignment, Color top, Color bottom){
            super(text, alignment);
            this.top = top;
            this.bottom = bottom;
        }

        /**
         * Inspired by http://stackoverflow.com/questions/14364291
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);

            super.paintComponent(g);
        }
    }

    /**
     * Custom image button with hover style.
     */
    private class HoverableImageButton extends JLabel{
        String iconName;

        HoverableImageButton(String filename, Dimension dimension){
            super();
            iconName = filename;
            setPreferredSize(dimension);
            setOpaque(true);
            setBackground(new Color(219, 219, 219));
            addMouseListener(new Events());
        }

        @Override
        public void setIcon(Icon icon){
            int width = getPreferredSize().width, height = getPreferredSize().height;
            super.setIcon(new ImageIcon(new ImageIcon("resources/" + iconName)
                    .getImage().getScaledInstance(
                            width > 0 ? width : 20,
                            height > 0 ? height : 20,
                            Image.SCALE_DEFAULT)));
        }

        @Override
        public void setPreferredSize(Dimension dimension){
            super.setPreferredSize(dimension);
            setIcon(null);
        }

        private class Events extends MouseAdapter{
            @Override
            public void mouseEntered(MouseEvent e){
                setBackground(new Color(181, 181, 181));
            }

            @Override
            public void mouseExited(MouseEvent e){
                setBackground(new Color(219, 219, 219));
            }
        }
    }

    /**
     * ScoreBar handles all score bar related things including scores and player names display.
     */
    private class ScoreBar{
        JLabel whitePlayerName, whitePlayerScore,
                blackPlayerName, blackPlayerScore;
        JPanel scoreBar;
        Side sideBeingCheck;

        ScoreBar(){
            init();
        }

        private void init(){
            // generate structure
            scoreBar = new JPanel(new GridLayout(1, 2));

            JPanel whitePlayer = new JPanel(new GridLayout(1, 2));
            JPanel blackPlayer = new JPanel(new GridLayout(1, 2));

            whitePlayerName = new GradientLabel(null, SwingConstants.LEFT,
                    new Color(248, 248, 248),
                    new Color(221, 221, 221)
            );
            whitePlayerScore = new GradientLabel(null, SwingConstants.RIGHT,
                    new Color(248, 248, 248),
                    new Color(221, 221, 221)
            );

            blackPlayerName = new GradientLabel(null, SwingConstants.RIGHT,
                    new Color(62, 62, 62),
                    new Color(28, 28, 28)
            );
            blackPlayerScore = new GradientLabel(null, SwingConstants.LEFT,
                    new Color(62, 62, 62),
                    new Color(28, 28, 28)
            );

            whitePlayer.add(whitePlayerName);
            whitePlayer.add(whitePlayerScore);
            blackPlayer.add(blackPlayerScore);
            blackPlayer.add(blackPlayerName);

            whitePlayer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            whitePlayer.addMouseListener(getPlayerChangeNameListenerAdapter(scoreKeeper.getPlayer(Side.WHITE)));
            blackPlayer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            blackPlayer.addMouseListener(getPlayerChangeNameListenerAdapter(scoreKeeper.getPlayer(Side.BLACK)));

            scoreBar.add(whitePlayer);
            scoreBar.add(blackPlayer);

            // fill in data
            update();
        }

        /**
         * Generate a player name change click listener for the specified Player.
         * @param player    The Player in which his name will be changed.
         * @return  MouseAdapter
         */
        private MouseAdapter getPlayerChangeNameListenerAdapter(ScoreKeeper.Player player){
            return new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    String oldName = player.name;
                    String newName = JOptionPane.showInputDialog("Enter a new name for " + oldName + ":", oldName);
                    if(newName != null) {
                        player.name = newName;
                        self.update();
                    }
                }
            };
        }

        /**
         * Get the panel for the generated structure.
         * @return  The root panel
         */
        public JPanel getPanel(){
            return scoreBar;
        }

        /**
         * Update the score bar with new information.
         */
        public void update(){
            whitePlayerName.setText("<html>" +
                    "<div style='width: 100%; font-size: 14px; font-family: Roboto; padding: 5px; color: black;'>" + scoreKeeper.getPlayer(Side.WHITE).name + "</div>" +
                    "</html>");
            whitePlayerScore.setText("<html>" +
                    "<div style='font-size: 18px; font-family: Roboto; padding-right: 10px;'>" +
                        (sideBeingCheck == Side.BLACK ? "<span style='font-size: 12px'>(Check)</span> " : "") + scoreKeeper.getPlayer(Side.WHITE).score +
                    "</div>" +
                    "</html>");
            blackPlayerName.setText( "<html>" +
                    "<div style='width: 100%; font-size: 14px; font-family: Roboto; padding: 5px; color: white;'>" + scoreKeeper.getPlayer(Side.BLACK).name + "</div>" +
                    "</html>");
            blackPlayerScore.setText("<html>" +
                    "<div style='font-size: 18px; font-family: Roboto; padding-left: 10px; color: white;'>" + scoreKeeper.getPlayer(Side.BLACK).score +
                    (sideBeingCheck == Side.WHITE ? " <span style='font-size: 12px'>(Check)</span>" : "") +
                    "</div>" +
                    "</html>");
        }

        public void setCheckSide(Side checkSide){
            sideBeingCheck = checkSide;
        }
    }
}
