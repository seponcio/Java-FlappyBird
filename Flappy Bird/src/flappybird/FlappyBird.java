package flappybird;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.Timer;
import javax.swing.JFrame;

public class FlappyBird implements ActionListener, MouseListener, KeyListener
{
	public static FlappyBird flappyBird;
	private static final int WIDTH = 1200;  
	private static final int HEIGHT = 800;
	private static final int REFRESH_RATE_MS = 40; 
	
	private Renderer renderer;
	private Rectangle bird;	
	private ArrayList<Rectangle> columns;
	private Random rand;
	private int ticks;
	private int yMotion;
	private int score;
	private GameState gameIs;
	
	//constructor for the FlappyBird class
	public FlappyBird()
	{		
		rand = new Random();
		this.gameIs = GameState.READY;
		
		setJFrame();		
		resetBird();
		
		columns = new ArrayList<>();
		
		for (int i =  1; i <= 4 ; i++)
		{
			addColumn(this.gameIs == GameState.READY);
		}
		
		Timer timer = new Timer(REFRESH_RATE_MS, this);	
		timer.start();
	}
	
	//program's starting point
	public static void main(String[] args)
	{
		flappyBird = new FlappyBird();
	}
	
	//sets-up the application's main JFrame
	private void setJFrame()
	{
		JFrame jframe = new JFrame();	
		this.renderer = new Renderer();
		jframe.add(this.renderer);
		jframe.setTitle("Flappy Bird");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(WIDTH, HEIGHT);
		jframe.addMouseListener(this);
		jframe.addKeyListener(this);
		jframe.setResizable(false);
		jframe.setVisible(true);
	}
	
	private void addColumn(boolean isStart)
	{
		int space 		= 300;
		int colWidth 	= 100;
		int colHeight 	= 50 + rand.nextInt(300);
		
		if (isStart)
		{
			//first walls appear further away into the right of the screen
			columns.add(new Rectangle(WIDTH + colWidth + columns.size() * 300, HEIGHT - colHeight - 120, colWidth, colHeight ));
			columns.add(new Rectangle(WIDTH + colWidth + (columns.size() - 1) * 300, 0, colWidth, HEIGHT - colHeight - space));
		}
		else
		{
			//later walls appear in regular intervals
			columns.add(new Rectangle(columns.get(columns.size() - 1).x + 600, HEIGHT - colHeight - 120, colWidth, colHeight ));
			columns.add(new Rectangle(columns.get(columns.size() - 1).x, 0, colWidth, HEIGHT - colHeight - space));
		}
	}
	
	private void paintColumn(Graphics g, Rectangle column)
	{
		g.setColor(Color.GREEN.darker());
		g.fillRect(column.x, column.y, column.width, column.height);
	}
	
	//action to perform each time the bird jumps
	private void jump()
	{		 
		if (this.gameIs == GameState.READY)
		{
			this.gameIs = GameState.STARTED;
		}
		else if (this.gameIs == GameState.STARTED)
		{
			if (yMotion > 0)
			{
				yMotion = 0;
			}
			yMotion -= 10;
		}		
		else if (this.gameIs == GameState.OVER)
		{
			resetBird();			
			columns.clear();
			yMotion = 0;
			score = 0;
			
			for (int i =  1; i <= 4 ; i++)
			{
				addColumn(true);
			}
			
			this.gameIs = GameState.READY;
		}
	 }
	
	private void resetBird()
	{
		final int cBirdSize = 20;
		final Point cBirdInitialPosition = new Point(WIDTH / 2 - cBirdSize / 2, HEIGHT / 2 - cBirdSize / 2);
		
		this.bird = new Rectangle(cBirdInitialPosition.x , cBirdInitialPosition.y, cBirdSize, cBirdSize);
	}
	 
	@Override
	//Defines the action to be performed by the ActionListener trigger
	//This action is triggered by the timer event
	public void actionPerformed(ActionEvent e)
	{
		if (this.gameIs == GameState.STARTED)
		{
			ticks++;	
			executeGameFrame();
		}
		renderer.repaint();
	}

	private void executeGameFrame()
	{
		adjustBirdPosition();		
		pushColumnsToTheLeft();		
		swapColumnsOverBorders();		
		maneuverBird();
	}

	private void maneuverBird()
	{
		int halfOfBird 		= this.bird.width / 2;
		int middleOfBird 	= this.bird.x + halfOfBird;
		
		for (Rectangle column : columns)
		{
			Boolean isTopColumn 	= column.y == 0;
			int halfOfColumn 		= column.width / 2;
			int middleOfColumn 		= column.x + halfOfColumn;
			
			if (	(isTopColumn)
				&& 	(middleOfBird > (middleOfColumn - halfOfBird))
				&& 	(middleOfBird < (middleOfColumn + halfOfBird)) 
				)
			{
				this.score++;
			}				
			else if (column.intersects(this.bird))
			{
				birdHitsWall(column);
			}
		}
		
		boolean birdTooHigh 	= this.bird.y < 0;
		boolean birdHitsGround 	= (this.bird.y + this.yMotion) >= (HEIGHT - 120);
		
		if (birdTooHigh || birdHitsGround)
		{
			this.gameIs = GameState.OVER;
		}
		
		if (birdHitsGround)
		{
			int groundLevel = HEIGHT - 120 - this.bird.height;
			this.bird.y 	= groundLevel;
		}
	}

	private void swapColumnsOverBorders()
	{
		for (int i = 0; i < this.columns.size(); i++)
		{
			Rectangle column = this.columns.get(i);	

			//remove columns once they cross the left border of the screen
			if (column.x + column.width < 0)
			{
				if (column.y == 0)
				{
					addColumn(false);
				}
				this.columns.remove(column);
			}
		}
	}

	private void pushColumnsToTheLeft()
	{
		final int speed = 10;
		
		//push all columns to the left
		for (int i = 0; i < this.columns.size(); i++)
		{
			Rectangle column = this.columns.get(i);	
			column.x -= speed; 
		}
	}

	private void adjustBirdPosition()
	{
		final int maxVerticalDropSpeed = 10;
		
		//calculate bird fall
		if (	(this.ticks % 2 == 0)
			&& 	(this.yMotion < maxVerticalDropSpeed)
			)
		{				
			this.yMotion += 2;
		}
		
		this.bird.y += this.yMotion;
	}
	
	private void birdHitsWall(Rectangle column)
	{
		boolean isHitFromLeft 		= this.bird.x <= column.x;
		boolean isHitFromAbove 		= column.y != 0;
		boolean isHitFromUnderneath = this.bird.y < column.height;
		
		this.gameIs = GameState.OVER;
		
		if (isHitFromLeft)
		{
			this.bird.x = column.x - this.bird.width;	
		}
		else if (isHitFromAbove)
		{
			this.bird.y = column.y - this.bird.height;
		}
		else if (isHitFromUnderneath)
		{
			this.bird.y = column.height;
		}	
	}
	
	
	public void repaint(Graphics g)
	{
		//Sky
		g.setColor(Color.CYAN);
		g.fillRect(0,0, WIDTH, HEIGHT);
		
		//dirt
		g.setColor(Color.ORANGE);
		g.fillRect(0, HEIGHT - 120, WIDTH, 120);
		
		//grass
		g.setColor(Color.GREEN);
		g.fillRect(0, HEIGHT - 120, WIDTH, 20);
		
		//bird
		g.setColor(Color.RED);
		g.fillRect(	this.bird.x, 
					this.bird.y,
					this.bird.width,
					this.bird.height
					);
		
		for (Rectangle column : columns)
		{
			paintColumn(g, column);
		}
		
		//hud
		final  String cFontName = "Arial";
		g.setColor(Color.WHITE);	
		g.setFont(new Font(cFontName, 1, 100));
		
		if (this.gameIs == GameState.OVER)
		{
			g.drawString("Game Over", 100, HEIGHT / 2 - 50);
		}
		else if (this.gameIs == GameState.READY)
		{
			g.drawString("Click to start!", 75, HEIGHT / 2 - 50);
		}		
		else if (this.gameIs == GameState.STARTED)
		{
			g.setFont(new Font(cFontName, 1, 50));
			g.drawString("Score: " + score, 25, 50);
		}
		
		//debugging info INI
		g.setFont(new Font(cFontName, 1, 20));
		g.setColor(Color.BLACK);
		g.drawString("Tick: " 					+ ticks, 			800, 50);
		g.drawString("yMotion: " 				+ yMotion, 			800, 70);
		g.drawString("Bird.y: " 				+ bird.y, 			800, 90);
		g.drawString("State: " 					+ gameIs, 			800, 110);
		g.drawString("columns array size: " 	+ columns.size(), 	800, 130);
		//debugging info END
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			jump();
		}
		else if (e.getKeyCode() == KeyEvent.VK_P)
		{
			if (this.gameIs == GameState.STARTED)
			{
				this.gameIs = GameState.PAUSED;
			}
			else if (this.gameIs == GameState.PAUSED)
			{
				this.gameIs = GameState.STARTED;
			}			
		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		jump();
	}

	@Override
	public void keyPressed(KeyEvent arg0)
	{
		//Keylistener not implemented for this interface
	}

	@Override
	public void keyTyped(KeyEvent arg0)
	{
		//Keylistener not implemented for this interface
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		//Mouselistener not implemented for this interface
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		//Mouselistener not implemented for this interface
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		//Mouselistener not implemented for this interface
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		//Mouselistener not implemented for this interface
	}

}
