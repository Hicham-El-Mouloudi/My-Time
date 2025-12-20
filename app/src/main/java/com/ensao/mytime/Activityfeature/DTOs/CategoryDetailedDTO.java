package com.ensao.mytime.Activityfeature.DTOs;

import androidx.room.Embedded;

import com.ensao.mytime.Activityfeature.Busniss.Category;

public class CategoryDetailedDTO {

    @Embedded
    public Category category;

    public String RepetitionTitle;
}
