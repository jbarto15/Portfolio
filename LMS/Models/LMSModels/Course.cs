using System;
using System.Collections.Generic;

namespace LMS.Models.LMSModels;

public partial class Course
{
    public uint CourseId { get; set; }

    public string Subject { get; set; } = null!;

    public ushort Number { get; set; }

    public string Name { get; set; } = null!;

    public virtual ICollection<Class> Classes { get; set; } = new List<Class>();

    public virtual Department SubjectNavigation { get; set; } = null!;
}
