using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using LMS.Models.LMSModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

// For more information on enabling MVC for empty projects, visit https://go.microsoft.com/fwlink/?LinkID=397860

namespace LMS_CustomIdentity.Controllers
{
    [Authorize(Roles = "Professor")]
    public class ProfessorController : Controller
    {

        private readonly LMSContext db;

        public ProfessorController(LMSContext _db)
        {
            db = _db;
        }

        public IActionResult Index()
        {
            return View();
        }

        public IActionResult Students(string subject, string num, string season, string year)
        {
            ViewData["subject"] = subject;
            ViewData["num"] = num;
            ViewData["season"] = season;
            ViewData["year"] = year;
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

        public IActionResult Categories(string subject, string num, string season, string year)
        {
            ViewData["subject"] = subject;
            ViewData["num"] = num;
            ViewData["season"] = season;
            ViewData["year"] = year;
            return View();
        }

        public IActionResult CatAssignments(string subject, string num, string season, string year, string cat)
        {
            ViewData["subject"] = subject;
            ViewData["num"] = num;
            ViewData["season"] = season;
            ViewData["year"] = year;
            ViewData["cat"] = cat;
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

        public IActionResult Submissions(string subject, string num, string season, string year, string cat, string aname)
        {
            ViewData["subject"] = subject;
            ViewData["num"] = num;
            ViewData["season"] = season;
            ViewData["year"] = year;
            ViewData["cat"] = cat;
            ViewData["aname"] = aname;
            return View();
        }

        public IActionResult Grade(string subject, string num, string season, string year, string cat, string aname, string uid)
        {
            ViewData["subject"] = subject;
            ViewData["num"] = num;
            ViewData["season"] = season;
            ViewData["year"] = year;
            ViewData["cat"] = cat;
            ViewData["aname"] = aname;
            ViewData["uid"] = uid;
            return View();
        }

        /*******Begin code to modify********/


        /// <summary>
        /// Returns a JSON array of all the students in a class.
        /// Each object in the array should have the following fields:
        /// "fname" - first name
        /// "lname" - last name
        /// "uid" - user ID
        /// "dob" - date of birth
        /// "grade" - the student's grade in this class
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <returns>The JSON array</returns>
        public IActionResult GetStudentsInClass(string subject, int num, string season, int year)
        {
            // Join together Student, Enrollment and Classes tables
            var students =
            from s in db.Students
            join e in db.Enrollments on s.UId equals e.StudentId
            join cl in db.Classes on e.ClassId equals cl.ClassId
            join co in db.Courses on cl.CourseId equals co.CourseId
            where co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
            select new {
                fname = s.FName,
                lname = s.LName,
                uid = s.UId,
                dob = s.Dob,
                grade = e.Grade
            };

            return Json( students.ToArray() );
        }



        /// <summary>
        /// Returns a JSON array with all the assignments in an assignment category for a class.
        /// If the "category" parameter is null, return all assignments in the class.
        /// Each object in the array should have the following fields:
        /// "aname" - The assignment name
        /// "cname" - The assignment category name.
        /// "due" - The due DateTime
        /// "submissions" - The number of submissions to the assignment
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class, 
        /// or null to return assignments from all categories</param>
        /// <returns>The JSON array</returns>
        public IActionResult GetAssignmentsInCategory(string subject, int num, string season, int year, string category)
        {
            // Join together Submissions, Assignment, Assignment Categories, Classes, and Courses
            var assignments =
            from asg in db.Assignments
            join ac in db.AssignmentCategories on asg.CategoryId equals ac.CategoryId
            join cl in db.Classes on ac.ClassId equals cl.ClassId
            join co in db.Courses on cl.CourseId equals co.CourseId
            where co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
            select new {
                aname = asg.Name,
                cname = ac.Name,
                due = asg.Due,
                submissions = db.Submissions.Count(sub => sub.AssignmentId == asg.AssignmentId) // Gets the number of submissions
            };

            // Filter by category if provided
            if ( !string.IsNullOrEmpty(category) ) {
                assignments = assignments.Where(a => a.cname == category);
            }

            var allAssignments = assignments.ToArray();

            return Json( allAssignments );
        }


        /// <summary>
        /// Returns a JSON array of the assignment categories for a certain class.
        /// Each object in the array should have the folling fields:
        /// "name" - The category name
        /// "weight" - The category weight
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class</param>
        /// <returns>The JSON array</returns>
        public IActionResult GetAssignmentCategories(string subject, int num, string season, int year)
        {
            var categories =
            from ac in db.AssignmentCategories
            join cl in db.Classes on ac.ClassId equals cl.ClassId
            join co in db.Courses on cl.CourseId equals co.CourseId
            where co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
            select new {
                name = ac.Name,
                weight = ac.Weight
            };

            return Json( categories.ToArray() );
        }

        /// <summary>
        /// Creates a new assignment category for the specified class.
        /// If a category of the given class with the given name already exists, return success = false.
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The new category name</param>
        /// <param name="catweight">The new category weight</param>
        /// <returns>A JSON object containing {success = true/false} </returns>
        public IActionResult CreateAssignmentCategory(string subject, int num, string season, int year, string category, int catweight)
        {
            // Find the class
            var classQuery =
            from cl in db.Classes
            join co in db.Courses on cl.CourseId equals co.CourseId
            where co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
            select cl;

            var classInstance = classQuery.FirstOrDefault();

            // If class is not found, return success = false
            if ( classInstance == null ) {
                return Json(new { success = false });
            }

            // Check if the assignment category already exists for the class
            var existingCategory =
            from ac in db.AssignmentCategories
            where ac.ClassId == classInstance.ClassId && ac.Name == category
            select ac;

            if ( existingCategory.Any() ) {
                return Json(new { success = false });
            }

            var newCategory = new AssignmentCategory {
                ClassId = classInstance.ClassId,
                Name = category,
                Weight = (ushort) catweight
            };

            db.AssignmentCategories.Add(newCategory);
            db.SaveChanges();

            return Json(new { success = true });
        }

        /// <summary>
        /// Creates a new assignment for the given class and category.
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class</param>
        /// <param name="asgname">The new assignment name</param>
        /// <param name="asgpoints">The max point value for the new assignment</param>
        /// <param name="asgdue">The due DateTime for the new assignment</param>
        /// <param name="asgcontents">The contents of the new assignment</param>
        /// <returns>A JSON object containing success = true/false</returns>
        public IActionResult CreateAssignment(string subject, int num, string season, int year, string category, string asgname, int asgpoints, DateTime asgdue, string asgcontents)
        {
            // Find the class
            var classQuery =
            from cl in db.Classes
            join co in db.Courses on cl.CourseId equals co.CourseId
            where co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
            select cl;

            var classInstance = classQuery.FirstOrDefault();

            // If class is not found, return success = false
            if ( classInstance == null ) {
                return Json(new { success = false });
            }

            // Find the assignment category
            var categoryQuery =
            from ac in db.AssignmentCategories
            where ac.ClassId == classInstance.ClassId && ac.Name == category
            select ac;

            var categoryInstance = categoryQuery.FirstOrDefault();

            // If category is not found, return success == false
            if ( categoryInstance == null ) {
                return Json(new { success = false });
            }

            // Check if the assignment already exists
            var existingAssignment =
            from asg in db.Assignments
            where asg.CategoryId == categoryInstance.CategoryId && asg.Name == asgname
            select asg;

            if ( existingAssignment.Any() ) {
                return Json(new { success = false });
            }

            var newAssignment = new Assignment {
                CategoryId = categoryInstance.CategoryId,
                Name = asgname,
                Points = (ushort) asgpoints,
                Due = asgdue,
                Contents = asgcontents
            };

            db.Assignments.Add(newAssignment);
            db.SaveChanges();

            return Json(new { success = true });
        }


        /// <summary>
        /// Gets a JSON array of all the submissions to a certain assignment.
        /// Each object in the array should have the following fields:
        /// "fname" - first name
        /// "lname" - last name
        /// "uid" - user ID
        /// "time" - DateTime of the submission
        /// "score" - The score given to the submission
        /// 
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class</param>
        /// <param name="asgname">The name of the assignment</param>
        /// <returns>The JSON array</returns>
        public IActionResult GetSubmissionsToAssignment(string subject, int num, string season, int year, string category, string asgname)
        {
            // Find the class
            var classQuery =
            from cl in db.Classes
            join co in db.Courses on cl.CourseId equals co.CourseId
            where co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
            select cl;

            var classInstance = classQuery.FirstOrDefault();

            // If class is not found, return success == false
            if ( classInstance == null ) {
                return Json(new { success = false, message = "Class not found" });
            }

            // Find the assignment category
            var categoryQuery =
            from ac in db.AssignmentCategories
            where ac.ClassId == classInstance.ClassId && ac.Name == category
            select ac;

            var categoryInstance = categoryQuery.FirstOrDefault();

            // If category is not found, return success == false
            if ( categoryInstance == null ) {
                return Json(new { success = false, message = "Category not found" });
            }

            // Find the assignment
            var assignmentQuery =
            from asg in db.Assignments
            where asg.CategoryId == categoryInstance.CategoryId && asg.Name == asgname
            select asg;

            var assignmentInstance = assignmentQuery.FirstOrDefault();

            // If assignment is not found, return false
            if ( assignmentInstance == null ) {
                return Json( new { success = false, message = "Assignment not found" } );
            }

            // Find the submissions
            var submissions =
            from sub in db.Submissions
            join s in db.Students on sub.StudentId equals s.UId
            where sub.AssignmentId == assignmentInstance.AssignmentId
            select new {
                fname = s.FName,
                lname = s.LName,
                uid = s.UId,
                time = sub.Time,
                score = sub.Score
            };

            return Json( submissions.ToArray() );

        }


        /// <summary>
        /// Set the score of an assignment submission
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class</param>
        /// <param name="asgname">The name of the assignment</param>
        /// <param name="uid">The uid of the student who's submission is being graded</param>
        /// <param name="score">The new score for the submission</param>
        /// <returns>A JSON object containing success = true/false</returns>
        public IActionResult GradeSubmission(string subject, int num, string season, int year, string category, string asgname, string uid, int score)
        {
            // Find the class
            var classInstance =
                (from cl in db.Classes
                join co in db.Courses on cl.CourseId equals co.CourseId
                where co.Subject == subject && co.Number == num && cl.Season == season && cl.Year == year
                select cl).FirstOrDefault();

            if ( classInstance == null ) {
                return Json(new { success = false });
            }

            // Find the assignment category
            var categoryInstance =
                (from ac in db.AssignmentCategories
                where ac.ClassId == classInstance.ClassId && ac.Name == category
                select ac).FirstOrDefault();

            if ( categoryInstance == null ) {
                return Json(new { success = false });
            }

            // Find the assignment
            var assignmentInstance =
                (from asg in db.Assignments
                where asg.CategoryId == categoryInstance.CategoryId && asg.Name == asgname
                select asg).FirstOrDefault();

            if ( assignmentInstance == null ) {
                return Json(new { success = false });
            }

            // Find the submission
            var submission =
                (from sub in db.Submissions
                where sub.AssignmentId == assignmentInstance.AssignmentId && sub.StudentId == uid
                select sub).FirstOrDefault();

            if ( submission != null ) {
                // Update the score
                submission.Score = (ushort) score;

            }
            else {
                // Create a new submission if it doesn't exist
                submission = new Submission {
                    AssignmentId = assignmentInstance.AssignmentId,
                    StudentId = uid,
                    Score = (ushort) score,
                    Time = DateTime.Now
                };

                db.Submissions.Add(submission);
            }

            db.SaveChanges();

            // Recalculate the student's grade
            CalculateStudentGrade(subject, num, season, year, uid);

            return Json( new { success = true } );

        }


        private void CalculateStudentGrade(string subject, int num, string season, int year, string uid)
        {

            // Get the class
            var classInstance = db.Classes
            .Join(db.Courses,
                cl => cl.CourseId,
                co => co.CourseId,
                (cl, co) => new { Class = cl, Course = co })
            .Where(cc => cc.Course.Subject == subject && cc.Course.Number == num && cc.Class.Season == season && cc.Class.Year == year)
            .Select(cc => cc.Class)
            .FirstOrDefault();

            if ( classInstance == null ) {
                return;
            }

            // Get the assignment categories for the class
            var categories = db.AssignmentCategories
                .Where(ac => ac.ClassId == classInstance.ClassId)
                .ToList();

            double totalWeightedScore = 0.0;
            double totalWeight = 0.0;

            foreach ( var category in categories ) {
                var assignments = db.Assignments
                    .Where(asg => asg.CategoryId == category.CategoryId)
                    .ToList();

                if ( !assignments.Any() ) {
                    continue;
                }

                double categoryPointsEarned = 0.0;
                double categoryMaxPoints = 0.0;

                foreach ( var assignment in assignments ) {
                    var submission = db.Submissions
                        .Where(sub => sub.AssignmentId == assignment.AssignmentId && sub.StudentId == uid)
                        .FirstOrDefault();

                    double score = submission?.Score ?? 0;
                    categoryPointsEarned += score;
                    categoryMaxPoints += assignment.Points;
                }

                if ( categoryMaxPoints > 0 ) {
                    double categoryPercentage = categoryPointsEarned / categoryMaxPoints;
                    double weightedCategoryScore = categoryPercentage * category.Weight;
                    totalWeightedScore += weightedCategoryScore;
                    totalWeight += category.Weight;
                }
            }

            if ( totalWeight > 0 ) {
                double scalingFactor = 100.0 / totalWeight;
                double totalPercentage = totalWeightedScore * scalingFactor;

                string letterGrade;
                if ( totalPercentage >= 93 ) {
                    letterGrade = "A";
                }
                else if ( totalPercentage >= 90 ) {
                    letterGrade = "A-";
                }
                else if ( totalPercentage >= 87 ) {
                    letterGrade = "B+";
                }
                else if ( totalPercentage >= 83 ) {
                    letterGrade = "B";
                }
                else if ( totalPercentage >= 80 ) {
                    letterGrade = "B-";
                }
                else if ( totalPercentage >= 77 ) {
                    letterGrade = "C+";
                }
                else if ( totalPercentage >= 73 ) {
                    letterGrade = "C";
                }
                else if ( totalPercentage >= 70 ) {
                    letterGrade = "C-";
                }
                else if ( totalPercentage >= 67 ) {
                    letterGrade = "D+";
                }
                else if ( totalPercentage >= 63 ) {
                    letterGrade = "D";
                }
                else if ( totalPercentage >= 60 ) {
                    letterGrade = "D-";
                }
                else {
                    letterGrade = "E";
                }

                // Get the enrollment instance
                var enrollment = db.Enrollments
                    .Where(e => e.ClassId == classInstance.ClassId && e.StudentId == uid)
                    .FirstOrDefault();

                // If the enrollment is not null, assign the new grade
                if ( enrollment != null ) {
                    enrollment.Grade = letterGrade;
                    db.SaveChanges();
                }
            }

        }



        /// <summary>
        /// Returns a JSON array of the classes taught by the specified professor
        /// Each object in the array should have the following fields:
        /// "subject" - The subject abbreviation of the class (such as "CS")
        /// "number" - The course number (such as 5530)
        /// "name" - The course name
        /// "season" - The season part of the semester in which the class is taught
        /// "year" - The year part of the semester in which the class is taught
        /// </summary>
        /// <param name="uid">The professor's uid</param>
        /// <returns>The JSON array</returns>
        public IActionResult GetMyClasses(string uid)
        {            
            // Join Professors, Classes, and Courses tables to find the classes taught by the professor
            var classes =
                from p in db.Professors
                join cl in db.Classes on p.UId equals cl.ProfessorId
                join co in db.Courses on cl.CourseId equals co.CourseId
                where p.UId == uid
                select new {
                    subject = co.Subject,
                    number = co.Number,
                    name = co.Name,
                    season = cl.Season,
                    year = cl.Year
                };

            return Json( classes.ToArray() );
        }

        
        /*******End code to modify********/
    }
}

