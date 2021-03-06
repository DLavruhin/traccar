/*
 * Copyright 2016 - 2017 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.api.resource;

import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.database.GroupsManager;
import org.traccar.model.Group;
import org.traccar.model.User;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

@Path("groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupResource extends BaseResource {

    @GET
    public Collection<Group> get(
            @QueryParam("all") boolean all, @QueryParam("userId") long userId) throws SQLException {
        GroupsManager groupsManager = Context.getGroupsManager();
        Set<Long> result = null;
        if (all) {
            if (Context.getPermissionsManager().isAdmin(getUserId())) {
                result = groupsManager.getAllItems();
            } else {
                Context.getPermissionsManager().checkManager(getUserId());
                result = groupsManager.getManagedItems(getUserId());
            }
        } else {
            if (userId == 0) {
                userId = getUserId();
            }
            Context.getPermissionsManager().checkUser(getUserId(), userId);
            result = groupsManager.getUserItems(userId);
        }
        return groupsManager.getItems(result);
    }

    @POST
    public Response add(Group entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        Context.getGroupsManager().addItem(entity);
        Context.getDataManager().linkObject(User.class, getUserId(), entity.getClass(), entity.getId(), true);
        Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
        Context.getPermissionsManager().refreshAllExtendedPermissions();
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @PUT
    public Response update(Group entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        Context.getPermissionsManager().checkGroup(getUserId(), entity.getId());
        Context.getGroupsManager().updateItem(entity);
        Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
        Context.getPermissionsManager().refreshAllExtendedPermissions();
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        Context.getPermissionsManager().checkGroup(getUserId(), id);
        Context.getGroupsManager().removeItem(id);
        Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
        Context.getPermissionsManager().refreshAllExtendedPermissions();
        return Response.noContent().build();
    }

}
