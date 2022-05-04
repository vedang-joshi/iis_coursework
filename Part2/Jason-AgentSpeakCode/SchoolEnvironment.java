import jason.environment.Environment;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import jason.asSyntax.*;
import java.awt.Color;
import java.util.logging.Logger;
import java.awt.Font;
import java.awt.Graphics;
import jason.environment.grid.GridWorldModel;
import java.util.Random;


public class SchoolEnvironment extends Environment {
	private ModelOfSchool schoolmodel;
    private ViewOfSchool  schoolview;
	
	static Logger log = Logger.getLogger(SchoolEnvironment.class.getName());

    public static final Term    nextPosition = Literal.parseLiteral("next(position)");
	public static final Term    studentEscape = Literal.parseLiteral("escape(student)");
	public static final Term    dropStudentToSafety = Literal.parseLiteral("leave_with(student)");
    public static final Term    pickUpPupil = Literal.parseLiteral("meet(student)");
	public static final Term	collisionDetection = Literal.parseLiteral("collide(wall)");
	public static final Literal	teacherCollidedWithWall = Literal.parseLiteral("collision_with_wall(teacher)");
    public static final Literal pupilsPickedUpByTeacher = Literal.parseLiteral("pupils_picked_up_by(teacher)");
    public static final Literal pupilsPickedUpBySecondTeacher = Literal.parseLiteral("pupils_picked_up_by(teacher2)");
	
	public static final int SizeOfGrid = 13; // size of grid
    public static final int PUPILS  = 16;// pupils code in the grid model
	public static final int WALLS  = 20;// pupils code in the grid model


    public void init(String[] args) {
        schoolmodel = new ModelOfSchool();
        schoolview  = new ViewOfSchool(schoolmodel);
        schoolmodel.setView(schoolview);
        updatingAllPercepts();
    }
	
	void updatingAllPercepts() {
        clearPercepts();

        Location positionOfTeacher = schoolmodel.getAgPos(0);
        Location positionOfSecondTeacher = schoolmodel.getAgPos(1);

        Literal currentPositionOfTeacher = Literal.parseLiteral("coordinates(teacher," + positionOfTeacher.x + "," + positionOfTeacher.y + ")");
        Literal currentPositionOfTeacher2 = Literal.parseLiteral("coordinates(teacher2," + positionOfSecondTeacher.x + "," + positionOfSecondTeacher.y + ")");

        addPercept(currentPositionOfTeacher);
        addPercept(currentPositionOfTeacher2);

        if (schoolmodel.hasObject(PUPILS, positionOfTeacher)) {
            addPercept(pupilsPickedUpByTeacher);
        }
		if (schoolmodel.hasObject(WALLS, positionOfTeacher)) {
            addPercept(teacherCollidedWithWall);
        }
        if (schoolmodel.hasObject(PUPILS, positionOfSecondTeacher)) {
            addPercept(pupilsPickedUpBySecondTeacher);
        }
    }
	
    public boolean executeAction(String teacherAgent, Structure nextAction) {
        log.info(teacherAgent+" performing action: "+ nextAction);
        try {
            if (nextAction.equals(nextPosition)) {
                schoolmodel.goToNextCell();
            } else if (nextAction.getFunctor().equals("moving_towards")) {
                int x = (int)((NumberTerm)nextAction.getTerm(0)).solve();
                int y = (int)((NumberTerm)nextAction.getTerm(1)).solve();
                schoolmodel.movingTowards(x,y);
            } else if (nextAction.getFunctor().equals("move_away")) {
                int x = (int)((NumberTerm)nextAction.getTerm(0)).solve();
                int y = (int)((NumberTerm)nextAction.getTerm(1)).solve();
                schoolmodel.moveAway(x,y);
            } else if (nextAction.equals(pickUpPupil)) {
                schoolmodel.pickPupilUp();
            } else if (nextAction.equals(dropStudentToSafety)) {
                schoolmodel.dropPupilOff();
            } else if (nextAction.equals(studentEscape)) {
                schoolmodel.Escape();
            } else if (nextAction.equals(collisionDetection)) {
                schoolmodel.collisionDetect();
            } else {
                return false;
            }
        } catch (Exception except) {
            except.printStackTrace();
        }

        updatingAllPercepts();

        try {
            Thread.sleep(100);
        } catch (Exception except) {}
        informAgsEnvironmentChanged();
        return true;
    }


    class ModelOfSchool extends GridWorldModel {
        boolean teacherShepherdingPupils = false; // check whether teacher is shepherding pupils or not
		// 
		int randomNumGenerator() {
			int min = 0;
			int max = 8;
			//Generate random int value from 0 to 8 
			int random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
			return random_int;
			}
        private ModelOfSchool() {
            super(SizeOfGrid, SizeOfGrid, 2);
            // initial location of teachers and randomise Fire position
            try {
                setAgPos(0, 0, 0);

                Location positionOfSecondTeacher = new Location(8, 3);
                setAgPos(1, positionOfSecondTeacher);
            } catch (Exception except) {
                except.printStackTrace();
            }
			
			int random_int_1 = randomNumGenerator();
			int random_int_2 = randomNumGenerator();
			int random_int_3 = randomNumGenerator();
			int random_int_4 = randomNumGenerator();
			int random_int_5 = randomNumGenerator();
			int random_int_6 = randomNumGenerator();
			int random_int_7 = randomNumGenerator();
			int random_int_8 = randomNumGenerator();
			int random_int_9 = randomNumGenerator();
			int random_int_10 = randomNumGenerator();
			int random_int_11 = randomNumGenerator();
			int random_int_12 = randomNumGenerator();
			int random_int_13 = randomNumGenerator();
			int random_int_14 = randomNumGenerator();


            // randomise initial location of pupils
            add(PUPILS, random_int_1, random_int_2);
            add(PUPILS, random_int_3, random_int_4);
            add(PUPILS, random_int_5, random_int_6);
            add(PUPILS, random_int_7, random_int_8);
            add(PUPILS, random_int_9, random_int_10);
            add(PUPILS, random_int_11, random_int_12);
			
			add(WALLS, random_int_13, random_int_14);
        }

        void goToNextCell() throws Exception {
            Location teacher = getAgPos(0);
            teacher.y++;
            if (teacher.y == getWidth()) {
                teacher.y = 0;
                teacher.x++;
            }
            // if teacher has finished searching the grid for all students:
            if (teacher.y == getHeight()) {
                return;
            }
            setAgPos(0, teacher);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }

        void movingTowards(int x, int y) throws Exception {
            Location teacher = getAgPos(0);
            if (teacher.x < x)
                teacher.x++;
            else if (teacher.x > x)
                teacher.x--;
            if (teacher.y < y)
                teacher.y++;
            else if (teacher.y > y)
                teacher.y--;
            setAgPos(0, teacher);
            setAgPos(1, getAgPos(1));
        }
		void moveAway(int x, int y) throws Exception {
            Location teacher = getAgPos(0);
			
			teacher.y++;
			if (teacher.y == getWidth()) {
                teacher.y = 0;
                teacher.x++;
            }
            // if teacher has finished searching the grid for all students:
            if (teacher.y == getHeight()) {
                return;
            }
            setAgPos(0, teacher);
            setAgPos(1, getAgPos(1));
        }
        void pickPupilUp() {
            // if the teacher location has pupils then remove the pupil object
            if (schoolmodel.hasObject(PUPILS, getAgPos(0))) {
				remove(PUPILS, getAgPos(0));
				teacherShepherdingPupils = true;
            }
        }
		void collisionDetect() {
            // if the teacher location has walls then mark as collision
			if (schoolmodel.hasObject(WALLS, getAgPos(0))) {
                remove(WALLS, getAgPos(0));
            }
        }
        void dropPupilOff() {
            if (teacherShepherdingPupils) {
                teacherShepherdingPupils = false;
                add(PUPILS, getAgPos(0));
            }
        }
        void Escape() {
            // if the teacher2 location has pupils
            if (schoolmodel.hasObject(PUPILS, getAgPos(1))) {
                remove(PUPILS, getAgPos(1));
            }
        }
    }

    class ViewOfSchool extends GridWorldView {
        public ViewOfSchool(ModelOfSchool schoolmodel) {
            super(schoolmodel, "Multi-agent simulation: School Evacuations", 800);
            defaultFont = new Font("Times New Roman", Font.BOLD, 12); // change default font
            setVisible(true);
        }
		public void draw(Graphics graphics, int xvalue, int yvalue, int studentobject) {
            switch (studentobject) {
            case SchoolEnvironment.PUPILS:
                super.drawObstacle(graphics, xvalue, yvalue);
				graphics.setColor(Color.white);
				drawString(graphics, xvalue, yvalue, defaultFont, "Student");
                break;
          
            case SchoolEnvironment.WALLS:
                super.drawObstacle(graphics, xvalue, yvalue);
				graphics.setColor(Color.black);
				drawString(graphics, xvalue, yvalue, defaultFont, "WALL");
                break;
            }
        }

        public void drawAgent(Graphics graphics, int xvalue, int yvalue, Color setColour, int count) {
            String strLabel = "Teacher"+" "+(count+1);
            setColour = Color.blue;
			
            if (count == 0) {
                setColour = Color.red;
                if (((ModelOfSchool)schoolmodel).teacherShepherdingPupils) {
                    strLabel += " picks Student";
                    setColour = Color.green;
                }
            }
			super.drawAgent(graphics, xvalue, yvalue, setColour, -1);
            if (count == 0) {
                graphics.setColor(Color.black);
            } else {
                graphics.setColor(Color.white);
            }
            super.drawString(graphics, xvalue, yvalue, defaultFont, strLabel);
            repaint();
        }
    }
}
