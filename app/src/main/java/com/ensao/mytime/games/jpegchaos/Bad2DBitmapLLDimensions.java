package com.aurora.myapplication;

public class Bad2DBitmapLLDimensions extends RuntimeException {
    public Bad2DBitmapLLDimensions(String message) {
        super(message);
    }
    public Bad2DBitmapLLDimensions(){super("Bad 2D Linked Bitmap List Dimensions ");}
    public Bad2DBitmapLLDimensions(int w , int h ){super("Bad 2D Linked Bitmap List Dimensions "+w+"x"+h);}

}
