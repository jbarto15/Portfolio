# Attempted vertical seekbar popup menu stuff

```kotlin
//private fun sliderMenu(v: View) {
//
//    val inflater = LayoutInflater.from(requireContext())
//    val sliderPopupView = inflater.inflate(R.layout.slider_popup, null)
//
//    val popupWindow = PopupWindow(
//        sliderPopupView,
//        ViewGroup.LayoutParams.WRAP_CONTENT,
//        ViewGroup.LayoutParams.WRAP_CONTENT,
//        true // Let the popup be dismissed
//    )
//
//    var seekBar = sliderPopupView.findViewById<SeekBar>(R.id.seekBar)
//
//    seekBar.setOnClickListener {
//        updateSlider(sliderPopupView)
//        //popupWindow.dismiss()
//    }
//
//    sliderPopupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED) // unspecified ensures that the
//    //val popupHeight = sliderPopupView.measuredHeight
//    val popupHeight = 100
//
//    val xOffset = 5
//    val yOffset = -(popupHeight + v.height) - 50 // Move the popup above the button
//
//    popupWindow.showAsDropDown(v, xOffset, yOffset)
//
//}
//
//private fun updateSlider(vieww: View) {
//
//    val brushSizeButtonInflater = LayoutInflater.from(requireContext())
//    val view = brushSizeButtonInflater.inflate(R.layout.slider_popup, null)
//
//    val seekBar: SeekBar = vieww.findViewById(R.id.seekBar)
//
//    seekBar.min = 0
//    seekBar.max = 100
//
//    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//
//        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//            studioVM.setBrushSize(87.5f)
//            studioVM.setSlider(progress)
//        }
//
//        override fun onStartTrackingTouch(seekBar: SeekBar?) { }
//
//        override fun onStopTrackingTouch(seekBar: SeekBar?) { }
//
//    })
//
//    //val dialogBox = AlertDialog.Builder(requireActivity())
//    //
//    //dialogBox.setTitle("Brush Size")
//    //    .setView(view)
//    //    .setPositiveButton("OK") { dialog, _ ->
//    //        dialog.dismiss()
//    //    }
//    //    .setNegativeButton("CANCEL") { dialog, _ ->
//    //        dialog.cancel()
//    //    }
//    //
//    //dialogBox.create().show()
//
//}
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="8dp"
    android:background="@color/popup_menu_background">

    <SeekBar
        android:id="@+id/seekBArr"
        android:rotation="270"
        android:layout_width="match_parent"
        android:layout_height="48dp" />

</LinearLayout>
```

# Old StudioFragment#shareDoodle() separated into two functions (this is the second one)

```
private fun shareDoodle() {
    if ( !::currentDoodle.isInitialized ) {
        Toast.makeText(context, "You must save doodle first", Toast.LENGTH_SHORT).show()
    } else {
        val imageFile = File(currentDoodle.path)
        val uriToImage: Uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.msdpaint.fileprovider",
            imageFile
        )
        shareDoodle(uriToImage)
    }
}


private fun shareDoodle(uriToImage: Uri) {

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uriToImage)
        type = "image/png"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)

}
```

# Naming

If you try and name a getter for a public property with getX where X is the name of the variable, it 
will clash with the Kotlin generated getter signature because both will compile down to the same 
signature. You can either rename or make the property not public

# Logcat

Can use default logd, loge, etc. tab complete.

Can also define own
1. Settings -> Editor -> Live Templates
2. '+' to create new group called 'user'
3. logd for abbreviation
4. Log.d("FOO", "") for template
5. Set context for Kotlin

Configure Logcat Formatting Options in logcat window has a compact mode or can manually set stuff

Same window has a soft wrap

package:mine & level:error | tag:FOO to see all exceptions plus FOO tags

IN RUN CONFIGURATIONS: in Miscellaneous tab
+ Can set to clear logcat with each run OR prevent logcat from displaying automatically

# 

```kotlin
// A "View" component
class MyFragment : Fragment() {

    private lateinit var binding: FragmentMyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // XML Layout resource includes a button for user to press and a TextView
        // which displays the current
        binding = FragmentMyBinding.inflate(inflater, container, false)
        
        binding.incrementButton.observe(viewLifecycleOwner) {
            
        }

        return binding.root
    }
    
}
```

# Properties vs. Fields tipper

Interfaces can include properties (because they are methods, not values), but they look and behave 
like values. Interfaces can NOT include fields.

# Show Unsaved Stuff

```kotlin
private fun enableSafetyNet() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if ( currentDoodle.isDefault() ) {

                    val dialogBox = AlertDialog.Builder(requireActivity())
                    val editText = EditText(requireContext())

                    dialogBox.setTitle("Do you want to save this drawing?")
                        .setMessage("Enter the name of your drawing")
                        .setView(editText)
                        .setPositiveButton("Yes") { dialog, _ ->
                            saveDoodle(editText.text.toString())
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                            studioVM.resetModel()
                            dialog.cancel()
                        }

                    dialogBox.create().show()
                }
                else {
                    Log.d("BackButtonPressed", currentDoodle.doodleName)
                    isEnabled = false // disable the callback
                    studioVM.resetModel()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }

        })
    }
```

# Firebase DocRef stuff from demo

```kotlin
Text("Welcome ${user!!.email} with id: ${user!!.uid}")
var dataString by remember { mutableStateOf("")}
val db = Firebase.firestore
val collection = db.collection("demoCollection")
collection
    .get()
    .addOnSuccessListener { result ->
        val doc = result.firstOrNull()
        dataString ="${doc?.id} => ${doc?.data}"

        val newData = hashMapOf(
            "Beans" to 1234L
        )

        db.collection("demoCollection").add(newData)

        //val docRef: DocumentReference = db.collection("demoCollection").document(doc!!.id)
        //docRef.set(newData, SetOptions.merge())


    }
    .addOnFailureListener { exception ->
        Log.w("Uh oh", "Error getting documents.", exception)
    }

Row {
    Text(text = dataString)
}
```

# Old BrushSizeMenu

```kotlin
private fun showBrushSizeMenu() {

    val brushSizeMenuInflater = LayoutInflater.from(requireContext())
    val brushSizeMenu = brushSizeMenuInflater.inflate(R.layout.menu_brush_size, null)
    val seekBar: SeekBar = brushSizeMenu.findViewById(R.id.seekBar)

    seekBar.min = StudioViewModel.BRUSH_SIZE_MIN.toInt()
    seekBar.max = StudioViewModel.BRUSH_SIZE_MAX.toInt()

    studioVM.brushSize.observe(viewLifecycleOwner) {
        seekBar.progress = it.toInt()
    }

    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            studioVM.updateBrushSize(progress.toFloat())
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) { }

        override fun onStopTrackingTouch(seekBar: SeekBar?) { }

    })

    val dialogBox = AlertDialog.Builder(requireActivity())

    dialogBox.setTitle("Brush Size")
        .setView(brushSizeMenu)
        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        .setNegativeButton("CANCEL") { dialog, _ -> dialog.cancel() }

    dialogBox.create().show()

}
```