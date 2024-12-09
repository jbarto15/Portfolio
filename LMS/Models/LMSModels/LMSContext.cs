using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using Pomelo.EntityFrameworkCore.MySql.Scaffolding.Internal;

namespace LMS.Models.LMSModels;

public partial class LMSContext : DbContext
{
    public LMSContext()
    {
    }

    public LMSContext(DbContextOptions<LMSContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Administrator> Administrators { get; set; }

    public virtual DbSet<Assignment> Assignments { get; set; }

    public virtual DbSet<AssignmentCategory> AssignmentCategories { get; set; }

    public virtual DbSet<Class> Classes { get; set; }

    public virtual DbSet<Course> Courses { get; set; }

    public virtual DbSet<Department> Departments { get; set; }

    public virtual DbSet<Enrollment> Enrollments { get; set; }

    public virtual DbSet<Professor> Professors { get; set; }

    public virtual DbSet<Student> Students { get; set; }

    public virtual DbSet<Submission> Submissions { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        => optionsBuilder.UseMySql("name=LMS:LMSConnectionString", Microsoft.EntityFrameworkCore.ServerVersion.Parse("10.11.8-mariadb"));

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder
            .UseCollation("latin1_swedish_ci")
            .HasCharSet("latin1");

        modelBuilder.Entity<Administrator>(entity =>
        {
            entity.HasKey(e => e.UId).HasName("PRIMARY");

            entity.Property(e => e.UId)
                .HasMaxLength(8)
                .IsFixedLength()
                .HasColumnName("uID");
            entity.Property(e => e.Dob).HasColumnName("DOB");
            entity.Property(e => e.FName)
                .HasMaxLength(100)
                .HasColumnName("fName");
            entity.Property(e => e.LName)
                .HasMaxLength(100)
                .HasColumnName("lName");
        });

        modelBuilder.Entity<Assignment>(entity =>
        {
            entity.HasKey(e => e.AssignmentId).HasName("PRIMARY");

            entity.HasIndex(e => e.CategoryId, "FK_category_id");

            entity.HasIndex(e => e.Name, "unique_assignment_name").IsUnique();

            entity.Property(e => e.AssignmentId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("assignmentID");
            entity.Property(e => e.CategoryId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("categoryID");
            entity.Property(e => e.Contents).HasMaxLength(8192);
            entity.Property(e => e.Due).HasColumnType("datetime");
            entity.Property(e => e.Name).HasMaxLength(100);
            entity.Property(e => e.Points).HasColumnType("smallint(5) unsigned");

            entity.HasOne(d => d.Category).WithMany(p => p.Assignments)
                .HasForeignKey(d => d.CategoryId)
                .HasConstraintName("FK_category_id");
        });

        modelBuilder.Entity<AssignmentCategory>(entity =>
        {
            entity.HasKey(e => e.CategoryId).HasName("PRIMARY");

            entity.HasIndex(e => e.ClassId, "FK_class_id_2");

            entity.HasIndex(e => new { e.Name, e.ClassId }, "unique_name_class_id").IsUnique();

            entity.Property(e => e.CategoryId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("categoryID");
            entity.Property(e => e.ClassId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("classID");
            entity.Property(e => e.Name).HasMaxLength(100);
            entity.Property(e => e.Weight).HasColumnType("smallint(5) unsigned");

            entity.HasOne(d => d.Class).WithMany(p => p.AssignmentCategories)
                .HasForeignKey(d => d.ClassId)
                .HasConstraintName("FK_class_id_2");
        });

        modelBuilder.Entity<Class>(entity =>
        {
            entity.HasKey(e => e.ClassId).HasName("PRIMARY");

            entity.HasIndex(e => e.ProfessorId, "FK_professor_id");

            entity.HasIndex(e => new { e.CourseId, e.Year, e.Season }, "unique_course_year_season").IsUnique();

            entity.Property(e => e.ClassId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("classID");
            entity.Property(e => e.CourseId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("courseID");
            entity.Property(e => e.EndTime).HasColumnType("time");
            entity.Property(e => e.Location).HasMaxLength(100);
            entity.Property(e => e.ProfessorId)
                .HasMaxLength(8)
                .IsFixedLength()
                .HasColumnName("professorID");
            entity.Property(e => e.Season).HasColumnType("enum('Spring','Fall','Summer')");
            entity.Property(e => e.StartTime).HasColumnType("time");
            entity.Property(e => e.Year).HasColumnType("smallint(5) unsigned");

            entity.HasOne(d => d.Course).WithMany(p => p.Classes)
                .HasForeignKey(d => d.CourseId)
                .HasConstraintName("FK_course_id");

            entity.HasOne(d => d.Professor).WithMany(p => p.Classes)
                .HasForeignKey(d => d.ProfessorId)
                .HasConstraintName("FK_professor_id");
        });

        modelBuilder.Entity<Course>(entity =>
        {
            entity.HasKey(e => e.CourseId).HasName("PRIMARY");

            entity.HasIndex(e => new { e.Subject, e.Number }, "unique_department_number").IsUnique();

            entity.Property(e => e.CourseId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("courseID");
            entity.Property(e => e.Name).HasMaxLength(100);
            entity.Property(e => e.Number).HasColumnType("smallint(5) unsigned");
            entity.Property(e => e.Subject).HasMaxLength(4);

            entity.HasOne(d => d.SubjectNavigation).WithMany(p => p.Courses)
                .HasForeignKey(d => d.Subject)
                .HasConstraintName("FK_department_courses");
        });

        modelBuilder.Entity<Department>(entity =>
        {
            entity.HasKey(e => e.Subject).HasName("PRIMARY");

            entity.Property(e => e.Subject).HasMaxLength(4);
            entity.Property(e => e.Name).HasMaxLength(100);
        });

        modelBuilder.Entity<Enrollment>(entity =>
        {
            entity.HasKey(e => new { e.StudentId, e.ClassId })
                .HasName("PRIMARY")
                .HasAnnotation("MySql:IndexPrefixLength", new[] { 0, 0 });

            entity.ToTable("Enrollment");

            entity.HasIndex(e => e.ClassId, "FK_class_id");

            entity.Property(e => e.StudentId)
                .HasMaxLength(8)
                .IsFixedLength()
                .HasColumnName("studentID");
            entity.Property(e => e.ClassId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("classID");
            entity.Property(e => e.Grade).HasColumnType("enum('A','A-','B+','B','B-','C+','C','C-','D+','D','D-','E','X','WF','EW','EU','F')");

            entity.HasOne(d => d.Class).WithMany(p => p.Enrollments)
                .HasForeignKey(d => d.ClassId)
                .HasConstraintName("FK_class_id");

            entity.HasOne(d => d.Student).WithMany(p => p.Enrollments)
                .HasForeignKey(d => d.StudentId)
                .HasConstraintName("FK_student_id");
        });

        modelBuilder.Entity<Professor>(entity =>
        {
            entity.HasKey(e => e.UId).HasName("PRIMARY");

            entity.HasIndex(e => e.Department, "FK_department_professor");

            entity.Property(e => e.UId)
                .HasMaxLength(8)
                .IsFixedLength()
                .HasColumnName("uID");
            entity.Property(e => e.Department).HasMaxLength(4);
            entity.Property(e => e.Dob).HasColumnName("DOB");
            entity.Property(e => e.FName)
                .HasMaxLength(100)
                .HasColumnName("fName");
            entity.Property(e => e.LName)
                .HasMaxLength(100)
                .HasColumnName("lName");

            entity.HasOne(d => d.DepartmentNavigation).WithMany(p => p.Professors)
                .HasForeignKey(d => d.Department)
                .HasConstraintName("FK_department_professor");
        });

        modelBuilder.Entity<Student>(entity =>
        {
            entity.HasKey(e => e.UId).HasName("PRIMARY");

            entity.HasIndex(e => e.Major, "FK_department_student");

            entity.Property(e => e.UId)
                .HasMaxLength(8)
                .IsFixedLength()
                .HasColumnName("uID");
            entity.Property(e => e.Dob).HasColumnName("DOB");
            entity.Property(e => e.FName)
                .HasMaxLength(100)
                .HasColumnName("fName");
            entity.Property(e => e.LName)
                .HasMaxLength(100)
                .HasColumnName("lName");
            entity.Property(e => e.Major).HasMaxLength(4);

            entity.HasOne(d => d.MajorNavigation).WithMany(p => p.Students)
                .HasForeignKey(d => d.Major)
                .HasConstraintName("FK_department_student");
        });

        modelBuilder.Entity<Submission>(entity =>
        {
            entity.HasKey(e => new { e.StudentId, e.AssignmentId })
                .HasName("PRIMARY")
                .HasAnnotation("MySql:IndexPrefixLength", new[] { 0, 0 });

            entity.ToTable("Submission");

            entity.HasIndex(e => e.AssignmentId, "FK_assignment_id_2");

            entity.Property(e => e.StudentId)
                .HasMaxLength(8)
                .IsFixedLength()
                .HasColumnName("studentID");
            entity.Property(e => e.AssignmentId)
                .HasColumnType("int(10) unsigned")
                .HasColumnName("assignmentID");
            entity.Property(e => e.Contents).HasMaxLength(8192);
            entity.Property(e => e.Score).HasColumnType("smallint(5) unsigned");
            entity.Property(e => e.Time).HasColumnType("datetime");

            entity.HasOne(d => d.Assignment).WithMany(p => p.Submissions)
                .HasForeignKey(d => d.AssignmentId)
                .HasConstraintName("FK_assignment_id_2");

            entity.HasOne(d => d.Student).WithMany(p => p.Submissions)
                .HasForeignKey(d => d.StudentId)
                .HasConstraintName("FK_student_id_2");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
