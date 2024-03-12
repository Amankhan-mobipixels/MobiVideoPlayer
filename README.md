# Mobi Video Player
add maven in your project level gradle
````
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' 
		}
	}
}
````
add dependency in module level gradle
````
dependencies:
{
   implementation 'com.github.Amankhan-mobipixels:MobiVideoPlayer:1.0.5'
}

````
How to use:

        val list = ArrayList<String>()
        list.add("file path")
        list.add("file path")

        val intent = Intent(this, MobiVideoPlayer::class.java)
        intent.putExtra("position", 0)
        intent.putStringArrayListExtra("videoArrayList", list)
        startActivity(intent)
   
	
