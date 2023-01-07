package com.final_project.wallpaper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.final_project.wallpaper.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var navController: NavController? = null
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup Nav Controller
        navController = Navigation.findNavController(view)

        //Check if User is already Logged in
        if(firebaseAuth.currentUser == null){
            //If not logged in, create account
            binding.rgtText.text = "Creating New Account"
            firebaseAuth.signInAnonymously().addOnCompleteListener {
                if(it.isSuccessful){
                    //Account Created, redirect to homepage
                    binding.rgtText.text = "Account Created, Logging in"
                    navController!!.navigate(R.id.action_registerFragment_to_homeFragment)
                } else {
                    //Error
                    binding.rgtText.text = "Error : ${it.exception!!.message}"
                }
            }
        } else {
            //Logged in, redirect to homepage
            navController!!.navigate(R.id.action_registerFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}