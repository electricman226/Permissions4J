package me.james.permissions4j.permission;

import com.sun.istack.internal.*;
import java.util.*;
import me.james.permissions4j.*;
import me.james.permissions4j.permission.exceptions.*;

import static me.james.permissions4j.Permissions4J.*;

public class Group
{
    private static HashMap<String, Group> groups = new HashMap<>();
    private String id, name;
    private HashMap<String, User> members = new HashMap<>();
    private HashMap<Node, PermissionValue> permissions = new HashMap<>();
    private Group inherit;

    private Group( String id, String name, Group inherit )
    {
        this.id = id;
        this.name = name;
        this.inherit = inherit;
        if ( this.inherit == null ) //This is a base group, make sure we don't get null permissions without having an inheritable group to fallback to.
        {
            for ( Node n : Node.getNodes() )
            {
                permissions.put( n, PermissionValue.DENY );
            }
        }
    }

    public static Group createGroup( @NotNull String id, @NotNull String name, Group inherit )
    {
        Group g = new Group( id, name, inherit );
        groups.put( id, g );
        return g;
    }

    public static Group[] getGroups()
    {
        return groups.values().toArray( new Group[groups.values().size()] );
    }

    public static Group getGroup( String id ) throws MissingGroupException
    {
        if ( !groups.containsKey( id ) )
            throw new MissingGroupException( id );
        return groups.get( id );
    }

    public Group setNode( Node n, PermissionValue value )
    {
        permissions.put( n, value );
        return this;
    }

    public void addMember( User user )
    {
        members.put( user.getName(), user );
    }

    public void removeMember( String name )
    {
        members.remove( name );
    }

    public String getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Group getInherit()
    {
        return inherit;
    }

    public User[] getMembers()
    {
        return members.values().toArray( new User[members.values().size()] );
    }

    public User getMember( String id )
    {
        return ( members.get( id ) );
    }

    public boolean hasPermission( Node n )
    {
        if ( permissions.get( n ) == null )
        {
            if ( inherit == null ) //Oh no, base group has no definitive permission, this should NEVER happen.
            {
                Permissions4J.getLogger().warning( String.format( "Group '%s' has is a base group without a definitive permission! (Node '%s')", getName(), n.getName() ) );
                return false;
            }
            return inherit.hasPermission( n );
        }
        return permissions.get( n ) == PermissionValue.ALLOW;
    }
}