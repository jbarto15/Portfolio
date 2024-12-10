using System;
using System.Collections.Generic;

namespace LMS.Models.LMSModels;

public partial class AssignmentCategory
{
    public uint CategoryId { get; set; }

    public uint ClassId { get; set; }

    public string Name { get; set; } = null!;

    public ushort Weight { get; set; }

    public virtual ICollection<Assignment> Assignments { get; set; } = new List<Assignment>();

    public virtual Class Class { get; set; } = null!;
}
