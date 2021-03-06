/*
 * *** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2017
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * *** END LICENSE BLOCK *****
 */

package org.dcm4chee.arc.stgcmt.rs;

import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.arc.conf.StorageVerificationPolicy;
import org.dcm4chee.arc.qmgt.HttpServletRequestInfo;
import org.dcm4chee.arc.stgcmt.StgCmtContext;
import org.dcm4chee.arc.stgcmt.StgCmtManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Sep 2016
 */
@RequestScoped
@Path("aets/{aet}/rs")
public class StgVerRS {
    private static final Logger LOG = LoggerFactory.getLogger(StgVerRS.class);

    @Inject
    private Device device;

    @Inject
    private StgCmtManager stgCmtMgr;

    @Inject
    private Event<StgCmtContext> stgCmtEvent;

    @PathParam("aet")
    private String aet;

    @Context
    private HttpServletRequest request;

    @QueryParam("storageVerificationPolicy")
    @Pattern(regexp = "DB_RECORD_EXISTS|OBJECT_EXISTS|OBJECT_SIZE|OBJECT_FETCH|OBJECT_CHECKSUM|S3_MD5SUM")
    private String storageVerificationPolicy;

    @QueryParam("storageVerificationUpdateLocationStatus")
    @Pattern(regexp = "true|false")
    private String storageVerificationUpdateLocationStatus;

    @QueryParam("storageVerificationStorageID")
    private List<String> storageVerificationStorageIDs;

    @POST
    @Path("/studies/{StudyInstanceUID}/stgver")
    @Produces("application/dicom+json,application/json")
    public StreamingOutput studyStorageCommit(
            @PathParam("StudyInstanceUID") String studyUID) {
        return storageCommit(studyUID, null, null);
    }

    @POST
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/stgver")
    @Produces("application/dicom+json,application/json")
    public StreamingOutput seriesStorageCommit(
            @PathParam("StudyInstanceUID") String studyUID,
            @PathParam("SeriesInstanceUID") String seriesUID) {
        return storageCommit(studyUID, seriesUID, null);
    }

    @POST
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}/stgver")
    @Produces("application/dicom+json,application/json")
    public StreamingOutput instanceStorageCommit(
            @PathParam("StudyInstanceUID") String studyUID,
            @PathParam("SeriesInstanceUID") String seriesUID,
            @PathParam("SOPInstanceUID") String sopUID) {
        return storageCommit(studyUID, seriesUID, sopUID);
    }

    private StreamingOutput storageCommit(String studyUID, String seriesUID, String sopUID) {
        LOG.info("Process POST {} from {}@{}", request.getRequestURI(), request.getRemoteUser(), request.getRemoteHost());
        StgCmtContext ctx = new StgCmtContext(getApplicationEntity(), aet)
                .setRequest(HttpServletRequestInfo.valueOf(request));
        if (storageVerificationPolicy != null)
            ctx.setStorageVerificationPolicy(StorageVerificationPolicy.valueOf(storageVerificationPolicy));
        if (storageVerificationUpdateLocationStatus != null)
            ctx.setStgCmtUpdateLocationStatus(Boolean.valueOf(storageVerificationUpdateLocationStatus));
        if (!storageVerificationStorageIDs.isEmpty())
            ctx.setStgCmtStorageIDs(storageVerificationStorageIDs.toArray(StringUtils.EMPTY_STRING));
        try {
            stgCmtMgr.calculateResult(ctx, studyUID, seriesUID, sopUID);
        } catch (IOException e) {
            ctx.setException(e);
            stgCmtEvent.fire(ctx);
            throw new WebApplicationException(e, errResponseAsTextPlain(e));
        }
        stgCmtEvent.fire(ctx);
        return out -> {
                try (JsonGenerator gen = Json.createGenerator(out)) {
                    JSONWriter writer = new JSONWriter(gen);
                    gen.writeStartArray();
                    writer.write(ctx.getEventInfo());
                    gen.writeEnd();
                }
        };
    }

    private Response errResponseAsTextPlain(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionAsString).type("text/plain").build();
    }

    private ApplicationEntity getApplicationEntity() {
        ApplicationEntity ae = device.getApplicationEntity(aet, true);
        if (ae == null || !ae.isInstalled())
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"errorMessage\":\"" + "No such Application Entity: " + aet + "\"}")
                            .build());
        return ae;
    }
}
