/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 8/30/20 9:15 AM
 * Last modified 8/30/20 8:42 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.vicinity.android.DependencyInjections.SubComponents

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import dagger.BindsInstance
import dagger.Subcomponent
import net.geeksempire.vicinity.android.DependencyInjections.Modules.Network.NetworkCheckpointModule
import net.geeksempire.vicinity.android.DependencyInjections.Modules.Network.NetworkConnectionModule
import net.geeksempire.vicinity.android.DependencyInjections.Scopes.ActivityScope
import net.geeksempire.vicinity.android.MapConfiguration.Map.MapsOfSociety

@ActivityScope
@Subcomponent(modules = [NetworkConnectionModule::class, NetworkCheckpointModule::class])
interface NetworkSubDependencyGraph {

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance appCompatActivity: AppCompatActivity, @BindsInstance constraintLayout: ConstraintLayout): NetworkSubDependencyGraph
    }

    fun inject(mapsOfSociety: MapsOfSociety)

}