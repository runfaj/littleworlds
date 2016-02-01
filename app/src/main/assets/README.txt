--------------------------------------------------------------------------------------------------

Hi! Thanks for downloading Borders! I've put hundreds of hours into this app to make it as best as possible and provide an awesome level of customization to your phone. If you like the app, I'd really appreciate if you'd share the app with others and purchase the full version to help me with future app updates! I've included instructions below on how to make your own Borders theme to add even more customization. Thanks, and I hope you enjoy!!

-- Stuart Roskelley --
-- Creator of Borders --
-- All Rights Reserved, 2016 --

--------------------------------------------------------------------------------------------------


Custom Borders themes can be created very easily with just a few steps:

1)  Manifest File

    Create a manifest file (manifest.json) with the following:

      {
        title: "My Border",
        file_prefix: "my_border"
      }
   
    The file prefix will be what the app reads for the various segments, listed in the next step.
   
2)  Border Image Files

    Create individual files that will be placed into the various segments of the borders. Keep in mind, the free version of the app has half of the segments locked and will not display.

    Segments should all be named in the following convention: <file_prefix>_SLB.png
    
    All files should be a png type and the file prefix must match the name in the manifest. It is STRONGLY recommended that you make your image files as small as possible so android doesn't have any rendering issues.

    The _SLB is an abbreviation for the position of the image. Here's the full list:

    -------------------------
    | TLC | TLM | TRM | TRC |
    |     |-----------|     |
    |-----|           |-----|
    | S |               | S |
    | L |               | R |
    | T |               | T |
    |---|               |---|
    | S |               | S |
    | L |               | R |
    | M |               | M |
    |---|               |---|
    | S |               | S |
    | L |               | R |
    | B |               | B |
    |-----|           |-----|
    |     |-----------|     |
    | BLC | BLM | BRM | BRC |
    -------------------------
    
    TLC:  Top Left Corner
    TLM:  Top Left Middle
    TRM:  Top Right Middle
    TRC:  Top Right Corner
    
    BLC:  Bottom Left Corner
    BLM:  Bottom Left Middle
    BRM:  Bottom Right Middle
    BRC:  Bottom Right Corner
    
    SLM:  Side Left Middle
    SLT:  Side Left Top
    SLB:  Side Left Bottom
    
    SRM:  Side Right Middle
    SRT:  Side Right Top
    SRB:  Side Right Bottom
    
    You only need to include the image files you'd like to have appear in the theme. Included in this folder is a template I use for creating and exporting my themes.
    
3)  Border Preview Image

    While not required, it is strongly recommended to have a preview image for your border theme -- especially if you are planning on sharing this to others.
    
    A preview file should be named in the following format (very similar to the border image files):
    
    <file_prefix>_preview.png

4)  Packing

    If you are planning on sharing this theme, or moving it to another device, you can export the files in a zip archive, then change the extension from "zip" to "btheme".

    "btheme" is what the app can read and extract from. The contents should ONLY include the image files and the manifest - no folders. Other files can be included, but the app won't do anything with them (for example, this README.txt file).

5)  Other Notes

    If you are sharing your theme, please leave the README file with the Creator notes and rights left alone. If you like the app, please also share with others and consider buying the full version!