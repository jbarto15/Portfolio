﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using LMS.Models.LMSModels;
using Microsoft.AspNetCore.Mvc;

// For more information on enabling MVC for empty projects, visit https://go.microsoft.com/fwlink/?LinkID=397860

namespace LMS.Controllers
{
    public class CommonController : Controller
    {
        private readonly LMSContext db;

        public CommonController(LMSContext _db)
        {
            db = _db;
        }

        /*******Begin code to modify********/

        /// <summary>
        /// Retreive a JSON array of all departments from the database.
        /// Each object in the array should have a field called "name" and "subject",
        /// where "name" is the department name and "subject" is the subject abbreviation.
        /// </summary>
        /// <returns>The JSON array</returns>
        public IActionResult GetDepartments()
        {            
            var departments = db.Departments
                                .Select(dept => new { dept.Name, dept.Subject })
                                .ToList();

            return Json(departments);
        }



        /// <summary>
        /// Returns a JSON array representing the course catalog.
        /// Each object in the array should have the following fields:
        /// "subject": The subject abbreviation, (e.g. "CS")
        /// "dname": The department name, as in "Computer Science"
        /// "courses": An array of JSON objects representing the courses in the department.
        ///            Each field in this inner-array should have the following fields:
        ///            "number": The course number (e.g. 5530)
        ///            "cname": The course name (e.g. "Database Systems")
        /// </summary>
        /// <returns>The JSON array</returns>
        public IActionResult GetCatalog()
        {            
            var catalog = db.Departments
                            .Select(dept => new
                            {
                                dname = dept.Name,
                                subject = dept.Subject,
                                courses = dept.Courses
                                              .Select(course => new { course.Number, cname = course.Name })
                                              .ToList()
                            })
                            .ToList();

            return Json(catalog);

        }

        /// <summary>
        /// Returns a JSON array of all class offerings of a specific course.
        /// Each object in the array should have the following fields:
        /// "season": the season part of the semester, such as "Fall"
        /// "year": the year part of the semester
        /// "location": the location of the class
        /// "start": the start time in format "hh:mm:ss"
        /// "end": the end time in format "hh:mm:ss"
        /// "fname": the first name of the professor
        /// "lname": the last name of the professor
        /// </summary>
        /// <param name="subject">The subject abbreviation, as in "CS"</param>
        /// <param name="number">The course number, as in 5530</param>
        /// <returns>The JSON array</returns>
        public IActionResult GetClassOfferings(string subject, int number)
        {       

            var offerings = from cls in db.Classes
                            where cls.Course.Subject == subject && cls.Course.Number == number
                            select new
                            {
                                cls.Year,
                                cls.Season,
                                cls.Location,
                                start = cls.StartTime.ToString("hh\\:mm\\:ss"),
                                end = cls.EndTime.ToString("hh\\:mm\\:ss"),
                                fname = cls.Professor.FName,
                                lname = cls.Professor.LName
                            };

            var classOfferings = offerings.ToList();

            return Json(classOfferings);

        }

        /// <summary>
        /// This method does NOT return JSON. It returns plain text (containing html).
        /// Use "return Content(...)" to return plain text.
        /// Returns the contents of an assignment.
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class</param>
        /// <param name="asgname">The name of the assignment in the category</param>
        /// <returns>The assignment contents</returns>
        public IActionResult GetAssignmentContents(string subject, int num, string season, int year, string category, string asgname)
        {       

            var assignmentContent = (from cls in db.Classes
                                     join course in db.Courses on cls.CourseId equals course.CourseId
                                     join ac in db.AssignmentCategories on cls.ClassId equals ac.ClassId
                                     join a in db.Assignments on ac.CategoryId equals a.CategoryId
                                     where course.Subject == subject &&
                                           course.Number == num &&
                                           cls.Season == season &&
                                           cls.Year == year &&
                                           ac.Name == category &&
                                           a.Name == asgname
                                     select a.Contents)
                                     .FirstOrDefault();

            return Content(assignmentContent ?? string.Empty);

        }


        /// <summary>
        /// This method does NOT return JSON. It returns plain text (containing html).
        /// Use "return Content(...)" to return plain text.
        /// Returns the contents of an assignment submission.
        /// Returns the empty string ("") if there is no submission.
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class</param>
        /// <param name="asgname">The name of the assignment in the category</param>
        /// <param name="uid">The uid of the student who submitted it</param>
        /// <returns>The submission text</returns>
        public IActionResult GetSubmissionText(string subject, int num, string season, int year, string category, string asgname, string uid)
        {            
            
            var submissionText = (from cls in db.Classes
                                  join course in db.Courses on cls.CourseId equals course.CourseId
                                  join ac in db.AssignmentCategories on cls.ClassId equals ac.ClassId
                                  join a in db.Assignments on ac.CategoryId equals a.CategoryId
                                  join s in db.Submissions on a.AssignmentId equals s.AssignmentId
                                  where s.StudentId == uid &&
                                        a.Name == asgname &&
                                        ac.Name == category &&
                                        cls.Season == season &&
                                        cls.Year == year &&
                                        course.Subject == subject &&
                                        course.Number == num
                                  select s.Contents)
                                  .FirstOrDefault();

            return Content(submissionText ?? string.Empty);

        }


        /// <summary>
        /// Gets information about a user as a single JSON object.
        /// The object should have the following fields:
        /// "fname": the user's first name
        /// "lname": the user's last name
        /// "uid": the user's uid
        /// "department": (professors and students only) the name (such as "Computer Science") of the department for the user. 
        ///               If the user is a Professor, this is the department they work in.
        ///               If the user is a Student, this is the department they major in.    
        ///               If the user is an Administrator, this field is not present in the returned JSON
        /// </summary>
        /// <param name="uid">The ID of the user</param>
        /// <returns>
        /// The user JSON object 
        /// or an object containing {success: false} if the user doesn't exist
        /// </returns>
        public IActionResult GetUser(string uid)
        {           
            var professor = db.Professors
                              .Where(p => p.UId == uid)
                              .Select(p => new
                              {
                                  fname = p.FName,
                                  lname = p.LName,
                                  uid = p.UId,
                                  department = p.Department
                              })
                              .FirstOrDefault();

            if (professor != null)
            {
                return Json(professor);
            }

            var student = db.Students
                            .Where(s => s.UId == uid)
                            .Select(s => new
                            {
                                fname = s.FName,
                                lname = s.LName,
                                uid = s.UId,
                                department = s.Major
                            })
                            .FirstOrDefault();

            if (student != null)
            {
                return Json(student);
            }

            var administrator = db.Administrators
                                  .Where(a => a.UId == uid)
                                  .Select(a => new
                                  {
                                      fname = a.FName,
                                      lname = a.LName,
                                      uid = a.UId
                                  })
                                  .FirstOrDefault();

            if (administrator != null)
            {
                return Json(administrator);
            }

            return Json(new { success = false });

        }


        /*******End code to modify********/
    }
}

