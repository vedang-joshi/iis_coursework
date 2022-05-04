// Teacher picking up students to deliver them to teacher 2 waiting by the 
// emergency exit to take them to safety.
/* Initial goal */

!check(positions).

/* Initial beliefs */

atLocation(P) :- coordinates(P,XPos,YPos) & 
			coordinates(teacher,XPos,YPos).

/* Plans */

+!check(positions) : not pupils_picked_up_by(teacher) 
		<- next(position);
		!check(positions).
		
+!check(positions) : not collision_with_wall(teacher) 
		<- next(position);
		!check(positions).

+!check(positions).


@pupil_pick_up_by_teacher[atomic]
+pupils_picked_up_by(teacher) : not .desire(guide_student_to(teacher2))
		<- !guide_student_to(teacher2).

+!guide_student_to(R)
		<- // remember previous position
		?coordinates(teacher,XPos,YPos);
		-+coordinates(lastPosition,XPos,YPos);

		// guide pupils to teacher2
		!guide_pupil(student,R);

		// teacher goes back to previous location to keep searching for students 
		// to guide to safety.
		!atLocation(lastPosition);
		!check(positions).

+!guide_pupil(S,Final) : true
   <- !confirm_student_pick_up(S);
      !atLocation(Final);
      leave_with(S).

+!confirm_student_pick_up(S) : pupils_picked_up_by(teacher)
		<- meet(student);
		!confirm_student_pick_up(S).
		+!confirm_student_pick_up(_).

+!atLocation(Final) : atLocation(Final).

+!atLocation(Final) <- ?coordinates(Final,XPos,YPos);
           moving_towards(XPos,YPos);
           !atLocation(Final).


@collide_with_wall[atomic]
+collision_with_wall(teacher) : not .desire(has_collision_occurred(wall))
		<- !has_collision_occurred(wall).

+!has_collision_occurred(W)
		<- // remember last position
		?coordinates(teacher,XPos,YPos);
		-+coordinates(lastPosition,XPos,YPos);

		// detect collision with wall.
		!collision_aversion(W,lastPosition);
		!atLocation(lastPosition).
		!check(positions).

+!collision_aversion(wall,lastPosition) : collision_with_wall(teacher)
   <- move_away(XPos,YPos).

+!atLocation(Final) : atLocation(Final).

+!atLocation(Final) <- ?coordinates(Final,XPos,YPos);
           moving_towards(XPos,YPos);
           !atLocation(Final).
