package fr.valkya.modules;

import fr.valkya.modules.alert.CommandAlert;
import fr.valkya.modules.alert.CommandAlertRaw;
import fr.valkya.modules.find.CommandFind;
import fr.valkya.modules.list.CommandList;
import fr.valkya.modules.send.CommandSend;
import fr.valkya.modules.server.CommandServer;
import net.md_5.bungee.api.plugin.Plugin;

public class Modules extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandAlert() );
        getProxy().getPluginManager().registerCommand( this, new CommandAlertRaw() );
        getProxy().getPluginManager().registerCommand( this, new CommandFind() );
        getProxy().getPluginManager().registerCommand( this, new CommandList() );
        getProxy().getPluginManager().registerCommand( this, new CommandSend() );
        getProxy().getPluginManager().registerCommand( this, new CommandServer() );
    }
}
