package org.rril.bungeelogin.Tasks;

import org.bukkit.scheduler.BukkitRunnable;

import org.rril.bungeelogin.bungeelogin;

public class SaveTask extends BukkitRunnable{

	private bungeelogin plugin;
	
	public SaveTask(bungeelogin plugin){
		this.plugin = plugin;
	}
	
	public void run(){
		if(plugin.configFile.getBoolean("SaveTask.Enabled")){
			plugin.savePortalsData();
		}
	}
}
