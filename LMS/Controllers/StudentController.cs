using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using LMS.Models.LMSModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

// For more information on enabling MVC for empty projects, visit https://go.microsoft.com/fwlink/?LinkID=397860

namespace LMS.Controllers
{
    [Authorize(Roles = "Student")]
    public class StudentController : Controller
    {
        private LMSContext db;
        public StudentController(LMSContext _db)
        {
            db = _db;
        }

        public IActionResult Index()
        {
            return View();
        }

        public IActionResult Catalog()
        {
            return View();
        }

        public IActionResult Class(string subject, string num, string season, string year)
        {
            ViewData["subject"] = subject;
            ViewData["num"] = num;
            ViewData["season"] = season;
            ViewData["year"] = year;
            return View();
        }

        public IActionResult Assignment(string subject, string num, string season, string year, string cat, string aname)
        {
            ViewData["subject"] = subject;
            ViewData["num"] = num;
            ViewData["season"] = season;
            ViewData["year"] = year;
            ViewData["cat"] = cat;
            ViewData["aname"] = aname;
            return View();
        }


        public IActionResult ClassListings(string subject, string num)
        {
            System.Diagnostics.Debug.WriteLine(subject + num);
            ViewData["subject"] = subject;
            ViewData["num"] = num;
            return View();
        }


        /*******Begin code to modify********/

        /// <summary>
        /// Returns a JSON array of the classes the given student is enrolled in.
        /// Each object in the array should have the following fields:
        /// "subject" - The subject abbreviation of the class (such as "CS")
        /// "number" - The course number (such as 5530)
        /// "name" - The course name
        /// "season" - The season part of the semester
        /// "year" - The year part of the semester
        /// "grade" - The grade earned in the class, or "--" if one hasn't been assigned
        /// </summary>
        /// <param name="uid">The uid of the student</param>
        /// <returns>The JSON array</returns>
        public IActionResult GetMyClasses(string uid)
        {  
        
            // Get all the classes of the specified student
            // Join the Enrollments, Classes, and Courses tables so that every class the particular 
            // student is in is returned

            var classes =
            from e in db.Enrollments
            join cl in db.Classes on e.ClassId equals cl.ClassId into eJoinCl
            from cl in eJoinCl.DefaultIfEmpty()
            join co in db.Courses on cl.CourseId equals co.CourseId into clJoinCO
            from co in clJoinCO.DefaultIfEmpty()
            where uid == e.StudentId

            select new {
                subject = co.Subject,
                number = co.Number,
                name = co.Name,
                season = cl.Season,
                year = cl.Year,
                grade = e.Grade ?? "--"
            };

            return Json(classes.ToArray());
        }

        /// <summary>
        /// Returns a JSON array of all the assignments in the given class that the given student is enrolled in.
        /// Each object in the array should have the following fields:
        /// "aname" - The assignment name
        /// "cname" - The category name that the assignment belongs to
        /// "due" - The due Date/Time
        /// "score" - The score earned by the student, or null if the student has not submitted to this assignment.
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="uid"></param>
        /// <returns>The JSON array</returns>
        public IActionResult GetAssignmentsInClass(string subject, int num, string season, int year, string uid)
        {     

            // Get all assignments for a specific class
            var assignmentsQuery =
                from e in db.Enrollments
                join cl in db.Classes on e.ClassId equals cl.ClassId
                join co in db.Courses on cl.CourseId equals co.CourseId
                join ac in db.AssignmentCategories on cl.ClassId equals ac.ClassId
                join asg in db.Assignments on ac.CategoryId equals asg.CategoryId
                where e.StudentId == uid && co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
                select new {
                    Assignment = asg,
                    Category = ac
                };

            // Join with Submissions to include all assignments and their submissions if there is one
            var assignmentsWithSubmissions =
                from a in assignmentsQuery
                join s in db.Submissions
                on new { A = a.Assignment.AssignmentId, B = uid } equals new { A = s.AssignmentId, B = s.StudentId }
                into subs
                from sub in subs.DefaultIfEmpty()
                select new {
                    aname = a.Assignment.Name,
                    cname = a.Category.Name,
                    due = (DateTime?) a.Assignment.Due ?? DateTime.MinValue, 
                    score = sub != null ? (int?) sub.Score : null
                };

            return Json( assignmentsWithSubmissions.ToArray() );

        }



        /// <summary>
        /// Adds a submission to the given assignment for the given student
        /// The submission should use the current time as its DateTime
        /// You can get the current time with DateTime.Now
        /// The score of the submission should start as 0 until a Professor grades it
        /// If a Student submits to an assignment again, it should replace the submission contents
        /// and the submission time (the score should remain the same).
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class</param>
        /// <param name="asgname">The new assignment name</param>
        /// <param name="uid">The student submitting the assignment</param>
        /// <param name="contents">The text contents of the student's submission</param>
        /// <returns>A JSON object containing {success = true/false}</returns>
        public IActionResult SubmitAssignmentText(string subject, int num, string season, int year,
          string category, string asgname, string uid, string contents)
        {      
            // Find the assignment
            var assignment = (from e in db.Enrollments
                            join cl in db.Classes on e.ClassId equals cl.ClassId
                            join co in db.Courses on cl.CourseId equals co.CourseId
                            join ac in db.AssignmentCategories on cl.ClassId equals ac.ClassId
                            join asg in db.Assignments on ac.CategoryId equals asg.CategoryId
                            where e.StudentId == uid && co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year && ac.Name == category && asg.Name == asgname
                            select asg).FirstOrDefault();

            // Check if the assignment exists
            if ( assignment == null ) {
                return Json( new { success = false, message = "Assignment not found." } );
            }

            // Find the student's submission for the assignment
            var submission = db.Submissions
                            .FirstOrDefault(s => s.AssignmentId == assignment.AssignmentId && s.StudentId == uid);

            // If submission is null, create a new one
            if ( submission == null ) {
                submission = new Submission {
                    AssignmentId = assignment.AssignmentId,
                    StudentId = uid,
                    Contents = contents,
                    Time = DateTime.Now,
                    Score = 0
                };

                db.Submissions.Add(submission);
            }
            else {
                // Update existing submission
                submission.Contents = contents;
                submission.Time = DateTime.Now;
            }

            db.SaveChanges();

            return Json( new { success = true } );
        }


        /// <summary>
        /// Enrolls a student in a class.
        /// </summary>
        /// <param name="subject">The department subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester</param>
        /// <param name="year">The year part of the semester</param>
        /// <param name="uid">The uid of the student</param>
        /// <returns>A JSON object containing {success = {true/false}. 
        /// false if the student is already enrolled in the class, true otherwise.</returns>
        public IActionResult Enroll(string subject, int num, string season, int year, string uid)
        {       
            // Need to check if the student is already enrolled in specified class
            var existingEnrollment = 
            from e in db.Enrollments
            join cl in db.Classes on e.ClassId equals cl.ClassId into eJoinCl
            from cl in eJoinCl.DefaultIfEmpty()
            join co in db.Courses on cl.CourseId equals co.CourseId into clJoinCO
            from co in clJoinCO.DefaultIfEmpty()
            where uid == e.StudentId && co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year

            select e;

            bool alreadyEnrolled = existingEnrollment.Any();

            if ( alreadyEnrolled ) {
                return Json( new { success = false} );
            }
            else {

                // Find the class the student needs to be enrolled in
                var classToEnroll =
                from cl in db.Classes
                join co in db.Courses on cl.CourseId equals co.CourseId
                where co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
                select cl;

                var selectedClass = classToEnroll.FirstOrDefault();

                if ( selectedClass == null ) {
                    return Json( new { success = false } );
                }

                var newEnrollment = new Enrollment {
                    StudentId = uid,
                    ClassId = selectedClass.ClassId,
                    Grade = null // No grade at enrollment
                };

                db.Enrollments.Add(newEnrollment);
                db.SaveChanges();

                return Json( new { success = true } );

            }
            
        }



        /// <summary>
        /// Calculates a student's GPA
        /// A student's GPA is determined by the grade-point representation of the average grade in all their classes.
        /// Assume all classes are 4 credit hours.
        /// If a student does not have a grade in a class ("--"), that class is not counted in the average.
        /// If a student is not enrolled in any classes, they have a GPA of 0.0.
        /// Otherwise, the point-value of a letter grade is determined by the table on this page:
        /// https://advising.utah.edu/academic-standards/gpa-calculator-new.php
        /// </summary>
        /// <param name="uid">The uid of the student</param>
        /// <returns>A JSON object containing a single field called "gpa" with the number value</returns>
        public IActionResult GetGPA(string uid)
        {            

            // Get all the students grades from the enrollment table
            var grades = 
            from e in db.Enrollments
            where e.StudentId == uid
            select e.Grade;

            // If the student isn't enrolled in any classes the GPA will be zero
            if ( !grades.Any() ) {

                var gpa = 0.0;
                return Json( new { gpa } );

            }

            var numOfGrades = 0;

            double sum = 0;

            foreach ( string grade in grades ) {

                if ( grade == "--" || string.IsNullOrEmpty(grade) ) {
                    continue;
                }
                else {

                    numOfGrades++;

                    switch (grade)
                    {
                        case "A":
                            sum += 4.0;
                            break;
                        case "A-":
                            sum += 3.7;
                            break;
                        case "B+":
                            sum += 3.3;
                            break;
                        case "B":
                            sum += 3.0;
                            break;
                        case "B-":
                            sum += 2.7;
                            break;
                        case "C+":
                            sum += 2.3;
                            break;
                        case "C":
                            sum += 2.0;
                            break;
                        case "C-":
                            sum += 1.7;
                            break;
                        case "D+":
                            sum += 1.3;
                            break;
                        case "D":
                            sum += 1.0;
                            break;
                        case "D-":
                            sum += 0.7;
                            break;
                        case "E":
                            sum += 0.0;
                            break;
                        default:
                            numOfGrades--; // don't count invalid grades
                            break;
                    }
                }
            }

            var calculatedGPA = numOfGrades > 0 ? sum / numOfGrades : 0.0;

            return Json( new { gpa = calculatedGPA } );
        }
                
        /*******End code to modify********/

    }
}

