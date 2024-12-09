using System;
using System.Collections.Generic;

namespace LMS.Models.LMSModels;

public partial class Class
{
    public uint ClassId { get; set; }

    public uint CourseId { get; set; }

    public string ProfessorId { get; set; } = null!;

    public ushort Year { get; set; }

    public string Season { get; set; } = null!;

    public string Location { get; set; } = null!;

    public TimeOnly StartTime { get; set; }

    public TimeOnly EndTime { get; set; }

    public virtual ICollection<AssignmentCategory> AssignmentCategories { get; set; } = new List<AssignmentCategory>();

    public virtual Course Course { get; set; } = null!;

    public virtual ICollection<Enrollment> Enrollments { get; set; } = new List<Enrollment>();

    public virtual Professor Professor { get; set; } = null!;
}
