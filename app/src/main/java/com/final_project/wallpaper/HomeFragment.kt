package com.final_project.wallpaper

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.model.Image
import com.example.finalproject.viewmodel.ImageViewModel
import com.final_project.wallpaper.databinding.FragmentHomeBinding
import kotlinx.coroutines.InternalCoroutinesApi


class HomeFragment : Fragment(), (Image) -> Unit {

    private val firebaseRepository = FirebaseRepository()
    private var navController: NavController? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private var wallpapersList: List<Image> = ArrayList()
    private val wallpapersListAdapter: WallpapersListAdapter = WallpapersListAdapter(wallpapersList, this)

    private var isLoading: Boolean = true

    var query1 = "MobileWallpaper"
    var sort = 0

    private val wallpapersViewModel: ImageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    //Searchview for searching a subreddit
    @OptIn(InternalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wallpapersViewModel.setup(query1,sort)
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query1 = query.toString()
                wallpapersViewModel.setup(query1,sort)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                Log.d("test2", query.toString())
                return false
            }
        })

        //Dropdown for sorting images
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    sort = position
                    wallpapersViewModel.setup(query1, sort)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //Action Bar
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar!!.title = "Wallpapers"

        //Nav Controller
        navController = Navigation.findNavController(view)

        //Check if User is logged in
        if(firebaseRepository.getUser() == null){
            //If user is not Logged in, Go to Register Page
            navController!!.navigate(R.id.action_homeFragment_to_registerFragment)
        }

        //Recycler View
        binding.wallpapersListView.setHasFixedSize(true)
        binding.wallpapersListView.layoutManager = GridLayoutManager(context, 2)
        binding.wallpapersListView.adapter = wallpapersListAdapter

        //Reached Bottom of RecyclerView
        binding.wallpapersListView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    //Reached bottom and not able to scroll anymore
                    if(!isLoading){
                        //Load Next Page
                        wallpapersViewModel.getNextImages()
                        isLoading = true

                        Log.d("HOME_FRAGMENT_LOG", "Reached Bottom: loading new content")
                    }
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        wallpapersViewModel.images.observe(viewLifecycleOwner, Observer {
            wallpapersList = it
            wallpapersListAdapter.wallpapersList = wallpapersList
            wallpapersListAdapter.notifyDataSetChanged()

            //Loading Complete
            isLoading = false
        })
    }

    override fun invoke(wallpaper: Image) {
        //Clicked on wallpaper from the list on homepage, navigate to Detail fragment
        Log.d("HOME_FRAGMENT_LOG", "Clicked on Item : ${wallpaper.id}")

        val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(wallpaper.imgUrl)
        navController!!.navigate(action)
    }
}