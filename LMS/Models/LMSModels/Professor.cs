using System;
using System.Collections.Generic;

namespace LMS.Models.LMSModels;

public partial class Professor
{
    public string UId { get; set; } = null!;

    public string FName { get; set; } = null!;

    public string LName { get; set; } = null!;

    public DateOnly Dob { get; set; }

    public string Department { get; set; } = null!;

    public virtual ICollection<Class> Classes { get; set; } = new List<Class>();

    public virtual Department DepartmentNavigation { get; set; } = null!;
}
