package fr.gphy.matbcih.tetris.tetristp;

/*
 Tetris Applet 
 by Melinda Green
 based on Guido Pellegrini's Summer 2000 term project

 Use this code for anything you like.
 If you use it in a mission critical application and 
 a bug in this code causes a global nuclear war, I will
 take full responsibility and will fix the bug for free.
 */
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
/*import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;*/

public class Tetris extends Applet {

	//
    // STATIC MEMBERS
    //
    public final static int INITIAL_DELAY = 1000;
    public final static byte ROWS = 18;
    public final static byte COLUMNS = 10;
    public final static int EMPTY = -1;
    public final static int EMPTY3 = -2;
    public final static int DELETED_ROWS_PER_LEVEL = 5;

    public final static Color PIECE_COLORS[] = {
        new Color(0xFF00FF),
        new Color(0xDC143C),
        new Color(0x00CED1),
        new Color(0xFFD700),
        new Color(0x32CD32),
        new Color(0x008080),
        new Color(0xFFA500),
        // fucia
    };
    //TODO : Ajouter des couleurs aux pieces, les données exadecimales vous sont fournies.
    
        
        // 0xDC143C  crimson
        // 0x00CED1  dark turquoise
        // 0xFFD700  gold
        // 0x32CD32  lime green
        // 0x008080  teal
        // 0xFFA500  orange
  

    public final static Color BACKGROUND_COLORS[] = {
        new Color(0xFFDAB9), // peachpuff
        new Color(0xFFC0CB), // pink
        new Color(0xFF99CC), // hot pink
        new Color(0x0099CC), // sky blue
        new Color(0x9966CC), // lavender
    };
    public final static Color BACKGROUND_COLOR = new Color(0x99FFCC);

	//   *    **   *    *    *    *
    //   *    *    *    **   **   **   **
    //   *    *    **    *   *    *    **
    //   *
    //   0    1    2    3    4    5    6   	
    public final static boolean PIECE_BITS[][][] = {
        {
            {false, true, false, false},
            {false, true, false, false},
            {false, true, false, false},
            {false, true, false, false},},
        {
            {false, false, false, false},
            {false, true, true, false},
            {false, true, false, false},
            {false, true, false, false},},
        {
            {false, false, false, false},
            {false, true, false, false},
            {false, true, false, false},
            {false, true, true, false},},
        {
            {false, false, false, false},
            {false, true, false, false},
            {false, true, true, false},
            {false, false, true, false},},
        {
            {false, false, false, false},
            {false, false, true, false},
            {false, true, true, false},
            {false, true, false, false},},
        {
            {false, false, false, false},
            {false, true, false, false},
            {false, true, true, false},
            {false, true, false, false},},
        {
            {false, false, false, false},
            {false, false, false, false},
            {false, true, true, false},
            {false, true, true, false},},};
    public static boolean tmp_grid[][] = new boolean[4][4]; // scratch space
    public static Random random = new Random();

    private static class TetrisLabel extends Label {

        private final static Font LABEL_FONT = new Font("Serif", Font.BOLD, 18);

        private TetrisLabel(String text) {
            super(text);
            setFont(LABEL_FONT);
        }

        private void addValue(int val) {
            setText(Integer.toString((Integer.parseInt(getText())) + val));
        }
    }

	//
    // INSTANCE DATA
    //
    public static int grid[][] = new int[ROWS][COLUMNS];
    private int next_piece_grid[][] = new int[4][4];
    public static int num_rows_deleted = 0;
    private GridCanvas game_grid = new GridCanvas(grid, true);
    private GridCanvas next_piece_canvas = new GridCanvas(next_piece_grid, false);
    public static Timer timer;
    private TetrisPiece cur_piece;
    private TetrisPiece next_piece = randomPiece();
    public static TetrisSound sounds;// = new TetrisSound(this);
    private TetrisLabel rows_deleted_label = new TetrisLabel("0");
    private TetrisLabel level_label = new TetrisLabel("1");
    private TetrisLabel score_label = new TetrisLabel("0");
    private TetrisLabel high_score_label = new TetrisLabel("");
    final Button start_newgame_butt = new TetrisButton("Start");
    final Button pause_resume_butt = new TetrisButton("Pause");

    private TetrisPiece randomPiece() {
        int rand = Math.abs(random.nextInt());
        return new TetrisPiece(rand % (PIECE_COLORS.length));
    }

    private void installNewPiece() {
        next_piece_canvas.clear();
        cur_piece = next_piece;
        cur_piece.setPosition(3, -4); //-4 to start above top of grid
        if (cur_piece.canPaste()) {
            next_piece = randomPiece();
            next_piece.setPosition(0, 0);
            next_piece.paste(next_piece_grid);
            next_piece_canvas.repaint();
        } else {
            gameOver();
        }
    }

    private void gameOver() {
        System.out.println("Game Over!");
        timer.setPaused(true);
        pause_resume_butt.setEnabled(false);
        int score = Integer.parseInt(score_label.getText());
        int high_score = high_score_label.getText().length() > 0
                ? Integer.parseInt(high_score_label.getText()) : 0;
        if (score > high_score) {
            high_score_label.setText("" + score);
        }
        sounds.playGameOverSound();
    }

    private boolean rowIsFull(int row) {
        for (int i = 0; i < COLUMNS; i++) {
            if (grid[row][i] == EMPTY) {
                return false;
            }
        }
        return true;
    }

    private int countFullRows() {
        int n_full_rows = 0;
        for (int i = 0; i < ROWS; i++) {
            if (rowIsFull(i)) {
                n_full_rows++;
            }
        }
        return n_full_rows;
    }

    private void removeRow(int row) {
        for (int j = 0; j < COLUMNS; j++) {
            grid[row][j] = EMPTY;
        }
        for (int i = row; i > 0; i--) {
            for (int j = 0; j < COLUMNS; j++) {
                grid[i][j] = grid[i - 1][j];
            }
        }
    }

    private void removeFullRows() {
        int n_full = countFullRows();
        score_label.addValue((int) (10 * Math.pow(2, n_full) - 10)); //give points exponentially
        if (n_full == 0) {
            return;
        }
        sounds.playDestroyRows(n_full);
        if (num_rows_deleted / DELETED_ROWS_PER_LEVEL != (num_rows_deleted + n_full) / DELETED_ROWS_PER_LEVEL) {
            timer.faster();
            level_label.addValue(n_full / DELETED_ROWS_PER_LEVEL + 1);
            level_label.repaint();
        }
        rows_deleted_label.addValue(n_full);
        num_rows_deleted += n_full;
        for (int i = ROWS - 1; i >= 0; i--) {
            while (rowIsFull(i)) {
                removeRow(i);
            }
        }
        game_grid.repaint();
    }

    public void start() {
        timer = new Timer(INITIAL_DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                synchronized (timer) {
                    if (cur_piece.canStepDown()) {
                        cur_piece.cut();
                        cur_piece.stepDown();
                        cur_piece.paste();
                        if (timer.isFast()) {
                            score_label.addValue(1); // a small reward for using fast mode
                        }
                    } else { // it hit something
                        timer.setFast(false);
                        if (!cur_piece.isTotallyOnGrid()) {
                            gameOver();
                        } else {
                            removeFullRows();
                            installNewPiece();
                        }
                    }
                }
                game_grid.repaint();
            }
        });
        timer.start(); // pauses immediately
    }

    public void stop() {
        pauseGame();
        synchronized (timer) {
            timer.stop();
        }
        timer = null;
    }

    private void startGame() {
        timer.setDelay(INITIAL_DELAY);
        timer.setPaused(false);
        start_newgame_butt.setLabel("Start New Game");
        pause_resume_butt.setEnabled(true); // stays enabled from here on
        pause_resume_butt.setLabel("Pause");
        pause_resume_butt.validate();
        sounds.playSoundtrack();
    }

    private void newGame() {
        game_grid.clear();
        installNewPiece();
        num_rows_deleted = 0;
        rows_deleted_label.setText("0");
        level_label.setText("1");
        score_label.setText("0");
        startGame();
    }

    private void pauseGame() {
        timer.setPaused(true);
        pause_resume_butt.setLabel("Resume");
        sounds.stopSoundtrack();
    }

    private void resumeGame() {
        timer.setPaused(false);
        pause_resume_butt.setLabel("Pause");
        sounds.playSoundtrack();
    }

   /* private void startSound() {
        try {
           
            URL f = new File("/tetris.wav").toURI().toURL();
            MediaLocator m = new MediaLocator(f.getClass().getResource("/tetris.wav"));//recuperation des resources dans l'arborescence maven
            Player p = Manager.createPlayer(m);
            p.start();
        } catch (Exception ex) {
            Logger.getLogger(Tetris.class.getName()).log(Level.SEVERE, null, ex);
        }

    }*/

    public void init() {
        sounds = new TetrisSound(); // NOTE: Must be initialized after Applet fully constructed!
        installNewPiece();

        pause_resume_butt.setEnabled(false);
        start_newgame_butt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (start_newgame_butt.getLabel().equals("Start")) {
                    startGame();
                    //startSound();
                } else {
                    newGame();
                }
            }
        });
        pause_resume_butt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (pause_resume_butt.getLabel().equals("Pause")) {
                    pauseGame();
                } else {
                    resumeGame();
                }
            }
        });

        //create key listener for rotating, moving left, moving right
        KeyListener key_listener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (timer.isPaused()) //don't do anything if game is paused
                {
                    return;
                }
                if (e.getKeyCode() == 37 || e.getKeyCode() == 39) { //left or right arrow pressed
                    int dir = e.getKeyCode() == 37 ? -1 : 1;
                    synchronized (timer) {
                        cur_piece.cut();
                        cur_piece.setX(cur_piece.getX() + dir); // try to move
                        if (!cur_piece.canPaste()) {
                            cur_piece.setX(cur_piece.getX() - dir); // undo move
                        }
                        cur_piece.paste();
                    }
                    game_grid.repaint();
                } else if (e.getKeyCode() == 38) { //rotate
                    synchronized (timer) {
                        cur_piece.cut();
                        cur_piece.rotate();
                        if (!cur_piece.canPaste()) {
                            cur_piece.rotateBack();
                        }
                        cur_piece.paste();
                    }
                    game_grid.repaint();
                }
                if (e.getKeyCode() == 40) { //down arrow pressed; drop piece
                    timer.setFast(true);
                }
            }
        };

		// add the key listener to all components that might get focus
        // so that it'll work regardless of which has focus
        start_newgame_butt.addKeyListener(key_listener);
        pause_resume_butt.addKeyListener(key_listener);

        Panel right_panel = new Panel(new GridLayout(3, 1));
        right_panel.setBackground(BACKGROUND_COLOR);

        Panel control_panel = new Panel();
        control_panel.add(start_newgame_butt);
        control_panel.add(pause_resume_butt);
        control_panel.setBackground(BACKGROUND_COLOR);
        right_panel.add(control_panel);

        Panel tmp = new Panel(new BorderLayout());
        tmp.add("North", new TetrisLabel("    Next Piece:"));
        tmp.add("Center", next_piece_canvas);
        tmp.setBackground(BACKGROUND_COLOR);
        right_panel.add(tmp);

        Panel stats_panel = new Panel(new GridLayout(4, 2));
        stats_panel.add(new TetrisLabel("    Rows Deleted: "));
        stats_panel.add(rows_deleted_label);
        stats_panel.add(new TetrisLabel("    Level: "));
        stats_panel.add(level_label);
        stats_panel.add(new TetrisLabel("    Score: "));
        stats_panel.add(score_label);
        stats_panel.add(new TetrisLabel("    High Score: "));
        stats_panel.add(high_score_label);
        tmp = new Panel(new BorderLayout());
        tmp.setBackground(BACKGROUND_COLOR);
        tmp.add("Center", stats_panel);
        right_panel.add(tmp);

        // finaly, add all the main panels to the applet panel
        this.setLayout(new GridLayout(1, 2));
        this.add(game_grid);
        this.add(right_panel);
        this.setBackground(BACKGROUND_COLOR);
        this.validate();
    }

    public static void main(String[] args) {

        Frame frame = new Frame("Tetris");
        Tetris tetris = new Tetris();
        frame.add(tetris);
        tetris.init();
        tetris.start();

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.setSize(489, 441);
        frame.setResizable(false);
        frame.setVisible(true);
    }
} // end class Tetris

