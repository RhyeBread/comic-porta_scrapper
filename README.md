# How to Get the Executable .jar File
The file will be in ComicPortaScrapper/dist/ComicPortaScrapper.jar. If you cannot open it, try downloading [jarfix](https://johann.loefflmann.net/en/software/jarfix/index.html).

# How to Use
After selecting the manga you want to download and clicking "Go to Manga", select the path to whichever folder you'd like. After that, select which chapter you'd like to download and click the "download" button. While downloading with the "All Chapters," option selected since it's all a background process, will download and stitch all of the images asynchronously; they won't be mixed up with each other, but double check nonetheless.

# Common Issues (As Far As I've Tested)
## Nothing Happening After a While
- This my be due to internet connection. However, it is possible that the download location had yet to be selected. Restart the application and select a location. Another way to make sure that this is the case is to open this with an IDE (I'd use Apache Net Beans since that's what I use) and check the terminal when downloading.

    Should nothing be printed inside of it, please submit a request or contact me directly on discord (rhyebread2) with an image of your screen, including the terminal in Apache/the IDE of your choice.

## Some Images Are in the Wrong Order
- This is an issue I don't think I'll be fixing any time soon, since--during my testing--I've ran into it ~1/10 runs. Try restarting the process and delete your images or just reorder the incorrect page/make a mental note of it.
