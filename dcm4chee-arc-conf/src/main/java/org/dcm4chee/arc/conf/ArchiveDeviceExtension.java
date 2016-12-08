/*
 * **** BEGIN LICENSE BLOCK *****
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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013
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
 * **** END LICENSE BLOCK *****
 */

package org.dcm4chee.arc.conf;

import org.dcm4che3.data.Code;
import java.time.LocalTime;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4che3.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Jul 2015
 */
public class ArchiveDeviceExtension extends DeviceExtension {

    private String defaultCharacterSet;
    private String fuzzyAlgorithmClass;
    private String storageID;
    private String metadataStorageID;
    private String seriesMetadataStorageID;
    private Duration seriesMetadataDelay;
    private Duration seriesMetadataPollingInterval;
    private int seriesMetadataFetchSize = 100;
    private Duration purgeInstanceRecordsDelay;
    private Duration purgeInstanceRecordsPollingInterval;
    private int purgeInstanceRecordsFetchSize = 100;
    private OverwritePolicy overwritePolicy;
    private ShowPatientInfo showPatientInfoInSystemLog;
    private ShowPatientInfo showPatientInfoInAuditLog;
    private String bulkDataSpoolDirectory;
    private String queryRetrieveViewID;
    private boolean sendPendingCGet = false;
    private Duration sendPendingCMoveInterval;
    private boolean personNameComponentOrderInsensitiveMatching = false;
    private int qidoMaxNumberOfResults = 0;
    private String wadoSR2HtmlTemplateURI;
    private String wadoSR2TextTemplateURI;
    private String patientUpdateTemplateURI;
    private String importReportTemplateURI;
    private String scheduleProcedureTemplateURI;
    private String unzipVendorDataToURI;
    private String[] mppsForwardDestinations = {};
    private String[] ianDestinations = {};
    private Duration ianDelay;
    private Duration ianTimeout;
    private boolean ianOnTimeout;
    private Duration ianTaskPollingInterval;
    private int ianTaskFetchSize = 100;
    private String fallbackCMoveSCP;
    private String fallbackCMoveSCPDestination;
    private String fallbackCMoveSCPLeadingCFindSCP;
    private int fallbackCMoveSCPRetries;
    private String externalRetrieveAEDestination;
    private String alternativeCMoveSCP;
    private Duration exportTaskPollingInterval;
    private int exportTaskFetchSize = 5;
    private Duration deleteRejectedPollingInterval;
    private int deleteRejectedFetchSize = 100;
    private Duration purgeStoragePollingInterval;
    private int purgeStorageFetchSize = 100;
    private int deleteStudyBatchSize = 10;
    private boolean deletePatientOnDeleteLastStudy = false;
    private Duration maxAccessTimeStaleness;
    private Duration aeCacheStaleTimeout;
    private Duration leadingCFindSCPQueryCacheStaleTimeout;
    private int leadingCFindSCPQueryCacheSize = 10;
    private String auditSpoolDirectory;
    private Duration auditPollingInterval;
    private Duration auditAggregateDuration;
    private String stowSpoolDirectory;
    private String wadoSpoolDirectory;
    private Duration purgeQueueMessagePollingInterval;
    private Duration purgeStgCmtPollingInterval;
    private Duration purgeStgCmtCompletedDelay;
    private SPSStatus[] hideSPSWithStatusFrom = {};
    private String hl7LogFilePattern;
    private String hl7ErrorLogFilePattern;
    private Duration rejectExpiredStudiesPollingInterval;
    private LocalTime rejectExpiredStudiesPollingStartTime;
    private int rejectExpiredStudiesFetchSize = 0;
    private int rejectExpiredSeriesFetchSize = 0;
    private String rejectExpiredStudiesAETitle;
    private String fallbackCMoveSCPStudyOlderThan;
    private String storePermissionServiceURL;
    private Pattern storePermissionServiceResponsePattern;
    private Pattern storePermissionServiceExpirationDatePattern;
    private Pattern storePermissionServiceErrorCommentPattern;
    private Pattern storePermissionServiceErrorCodePattern;
    private Duration storePermissionCacheStaleTimeout;
    private int storePermissionCacheSize = 10;
    private Duration mergeMWLCacheStaleTimeout;
    private int mergeMWLCacheSize = 10;
    private int storeUpdateDBMaxRetries = 1;
    private AllowRejectionForDataRetentionPolicyExpired allowRejectionForDataRetentionPolicyExpired;
    private AcceptMissingPatientID acceptMissingPatientID;
    private AllowDeleteStudyPermanently allowDeleteStudyPermanently;
    private String[] retrieveAETitles = {};

    private final HashSet<String> wadoSupportedSRClasses = new HashSet<>();
    private final EnumMap<Entity,AttributeFilter> attributeFilters = new EnumMap<>(Entity.class);
    private final Map<String,MetadataFilter> metadataFilters = new HashMap<>();
    private final EnumMap<IDGenerator.Name,IDGenerator> idGenerators = new EnumMap<>(IDGenerator.Name.class);
    private final Map<String, QueryRetrieveView> queryRetrieveViewMap = new HashMap<>();
    private final Map<String, StorageDescriptor> storageDescriptorMap = new HashMap<>();
    private final Map<String, QueueDescriptor> queueDescriptorMap = new HashMap<>();
    private final Map<String, ExporterDescriptor> exporterDescriptorMap = new HashMap<>();
    private final Map<String, RejectionNote> rejectionNoteMap = new HashMap<>();
    private final ArrayList<ExportRule> exportRules = new ArrayList<>();
    private final ArrayList<RSForwardRule> rsForwardRules = new ArrayList<>();
    private final ArrayList<HL7ForwardRule> hl7ForwardRules = new ArrayList<>();
    private final ArrayList<ArchiveCompressionRule> compressionRules = new ArrayList<>();
    private final ArrayList<StudyRetentionPolicy> studyRetentionPolicies = new ArrayList<>();
    private final ArrayList<ArchiveAttributeCoercion> attributeCoercions = new ArrayList<>();
    private final ArrayList<StoreAccessControlIDRule> storeAccessControlIDRules = new ArrayList<>();

    private transient FuzzyStr fuzzyStr;

    public String getDefaultCharacterSet() {
        return defaultCharacterSet;
    }

    public void setDefaultCharacterSet(String defaultCharacterSet) {
        this.defaultCharacterSet = defaultCharacterSet;
    }

    public String getFuzzyAlgorithmClass() {
        return fuzzyAlgorithmClass;
    }

    public void setFuzzyAlgorithmClass(String fuzzyAlgorithmClass) {
        this.fuzzyStr = fuzzyStr(fuzzyAlgorithmClass);
        this.fuzzyAlgorithmClass = fuzzyAlgorithmClass;
    }

    public FuzzyStr getFuzzyStr() {
        if (fuzzyStr == null)
            if (fuzzyAlgorithmClass == null)
                throw new IllegalStateException("No Fuzzy Algorithm Class configured");
            else
                fuzzyStr = fuzzyStr(fuzzyAlgorithmClass);
        return fuzzyStr;
    }

    private static FuzzyStr fuzzyStr(String s) {
        try {
            return (FuzzyStr) Class.forName(s).newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    public OverwritePolicy getOverwritePolicy() {
        return overwritePolicy;
    }

    public void setOverwritePolicy(OverwritePolicy overwritePolicy) {
        this.overwritePolicy = overwritePolicy;
    }

    public ShowPatientInfo getShowPatientInfoInSystemLog() {
        return showPatientInfoInSystemLog;
    }

    public ShowPatientInfo showPatientInfoInSystemLog() {
        return showPatientInfoInSystemLog != null ? showPatientInfoInSystemLog : ShowPatientInfo.PLAIN_TEXT;
    }

    public void setShowPatientInfoInSystemLog(ShowPatientInfo showPatientInfoInSystemLog) {
        this.showPatientInfoInSystemLog = showPatientInfoInSystemLog;
    }

    public ShowPatientInfo getShowPatientInfoInAuditLog() {
        return showPatientInfoInAuditLog;
    }

    public void setShowPatientInfoInAuditLog(ShowPatientInfo showPatientInfoInAuditLog) {
        this.showPatientInfoInAuditLog = showPatientInfoInAuditLog;
    }

    public ShowPatientInfo showPatientInfoInAuditLog() {
        return showPatientInfoInAuditLog != null ? showPatientInfoInAuditLog : ShowPatientInfo.PLAIN_TEXT;
    }

    public AcceptMissingPatientID getAcceptMissingPatientID() {
        return acceptMissingPatientID;
    }

    public void setAcceptMissingPatientID(AcceptMissingPatientID acceptMissingPatientID) {
        this.acceptMissingPatientID = acceptMissingPatientID;
    }

    public String getBulkDataSpoolDirectory() {
        return bulkDataSpoolDirectory;
    }

    public void setBulkDataSpoolDirectory(String bulkDataSpoolDirectory) {
        this.bulkDataSpoolDirectory = bulkDataSpoolDirectory;
    }

    public String getStorageID() {
        return storageID;
    }

    public void setStorageID(String storageID) {
        this.storageID = storageID;
    }

    public String getMetadataStorageID() {
        return metadataStorageID;
    }

    public void setMetadataStorageID(String metadataStorageID) {
        this.metadataStorageID = metadataStorageID;
    }

    public String getSeriesMetadataStorageID() {
        return seriesMetadataStorageID;
    }

    public void setSeriesMetadataStorageID(String seriesMetadataStorageID) {
        this.seriesMetadataStorageID = seriesMetadataStorageID;
    }

    public Duration getSeriesMetadataDelay() {
        return seriesMetadataDelay;
    }

    public void setSeriesMetadataDelay(Duration seriesMetadataDelay) {
        this.seriesMetadataDelay = seriesMetadataDelay;
    }

    public Duration getSeriesMetadataPollingInterval() {
        return seriesMetadataPollingInterval;
    }

    public void setSeriesMetadataPollingInterval(Duration seriesMetadataPollingInterval) {
        this.seriesMetadataPollingInterval = seriesMetadataPollingInterval;
    }

    public int getSeriesMetadataFetchSize() {
        return seriesMetadataFetchSize;
    }

    public void setSeriesMetadataFetchSize(int seriesMetadataFetchSize) {
        this.seriesMetadataFetchSize =  greaterZero(seriesMetadataFetchSize, "seriesMetadataFetchSize");
    }

    public Duration getPurgeInstanceRecordsDelay() {
        return purgeInstanceRecordsDelay;
    }

    public void setPurgeInstanceRecordsDelay(Duration purgeInstanceRecordsDelay) {
        this.purgeInstanceRecordsDelay = purgeInstanceRecordsDelay;
    }

    public Duration getPurgeInstanceRecordsPollingInterval() {
        return purgeInstanceRecordsPollingInterval;
    }

    public void setPurgeInstanceRecordsPollingInterval(Duration purgeInstanceRecordsPollingInterval) {
        this.purgeInstanceRecordsPollingInterval = purgeInstanceRecordsPollingInterval;
    }

    public int getPurgeInstanceRecordsFetchSize() {
        return purgeInstanceRecordsFetchSize;
    }

    public void setPurgeInstanceRecordsFetchSize(int purgeInstanceRecordsFetchSize) {
        this.purgeInstanceRecordsFetchSize =  greaterZero(purgeInstanceRecordsFetchSize, "purgeInstanceRecordsFetchSize");
    }

    public String getQueryRetrieveViewID() {
        return queryRetrieveViewID;
    }

    public void setQueryRetrieveViewID(String queryRetrieveViewID) {
        this.queryRetrieveViewID = queryRetrieveViewID;
    }

    public boolean isPersonNameComponentOrderInsensitiveMatching() {
        return personNameComponentOrderInsensitiveMatching;
    }

    public void setPersonNameComponentOrderInsensitiveMatching(boolean personNameComponentOrderInsensitiveMatching) {
        this.personNameComponentOrderInsensitiveMatching = personNameComponentOrderInsensitiveMatching;
    }

    public boolean isSendPendingCGet() {
        return sendPendingCGet;
    }

    public void setSendPendingCGet(boolean sendPendingCGet) {
        this.sendPendingCGet = sendPendingCGet;
    }

    public Duration getSendPendingCMoveInterval() {
        return sendPendingCMoveInterval;
    }

    public void setSendPendingCMoveInterval(Duration sendPendingCMoveInterval) {
        this.sendPendingCMoveInterval = sendPendingCMoveInterval;
    }

    public String[] getWadoSupportedSRClasses() {
        return wadoSupportedSRClasses.toArray(StringUtils.EMPTY_STRING);
    }

    public void setWadoSupportedSRClasses(String... wadoSupportedSRClasses) {
        this.wadoSupportedSRClasses.clear();
        this.wadoSupportedSRClasses.addAll(Arrays.asList(wadoSupportedSRClasses));
    }

    public boolean isWadoSupportedSRClass(String cuid) {
        return wadoSupportedSRClasses.contains(cuid);
    }

    public String getWadoSR2HtmlTemplateURI() {
        return wadoSR2HtmlTemplateURI;
    }

    public void setWadoSR2HtmlTemplateURI(String wadoSR2HtmlTemplateURI) {
        this.wadoSR2HtmlTemplateURI = wadoSR2HtmlTemplateURI;
    }

    public String getWadoSR2TextTemplateURI() {
        return wadoSR2TextTemplateURI;
    }

    public void setWadoSR2TextTemplateURI(String wadoSR2TextTemplateURI) {
        this.wadoSR2TextTemplateURI = wadoSR2TextTemplateURI;
    }

    public String getPatientUpdateTemplateURI() {
        return patientUpdateTemplateURI;
    }

    public void setPatientUpdateTemplateURI(String patientUpdateTemplateURI) {
        this.patientUpdateTemplateURI = patientUpdateTemplateURI;
    }

    public String getImportReportTemplateURI() {
        return importReportTemplateURI;
    }

    public void setImportReportTemplateURI(String importReportTemplateURI) {
        this.importReportTemplateURI = importReportTemplateURI;
    }

    public String getScheduleProcedureTemplateURI() {
        return scheduleProcedureTemplateURI;
    }

    public void setScheduleProcedureTemplateURI(String scheduleProcedureTemplateURI) {
        this.scheduleProcedureTemplateURI = scheduleProcedureTemplateURI;
    }

    public String getUnzipVendorDataToURI() {
        return unzipVendorDataToURI;
    }

    public void setUnzipVendorDataToURI(String unzipVendorDataToURI) {
        this.unzipVendorDataToURI = unzipVendorDataToURI;
    }

    public String[] getMppsForwardDestinations() {
        return mppsForwardDestinations;
    }

    public void setMppsForwardDestinations(String... mppsForwardDestinations) {
        this.mppsForwardDestinations = mppsForwardDestinations;
    }

    public String[] getIanDestinations() {
        return ianDestinations;
    }

    public void setIanDestinations(String... ianDestinations) {
        this.ianDestinations = ianDestinations;
    }

    public Duration getIanDelay() {
        return ianDelay;
    }

    public void setIanDelay(Duration ianDelay) {
        this.ianDelay = ianDelay;
    }

    public Duration getIanTimeout() {
        return ianTimeout;
    }

    public void setIanTimeout(Duration ianTimeout) {
        this.ianTimeout = ianTimeout;
    }

    public boolean isIanOnTimeout() {
        return ianOnTimeout;
    }

    public void setIanOnTimeout(boolean ianOnTimeout) {
        this.ianOnTimeout = ianOnTimeout;
    }

    public Duration getIanTaskPollingInterval() {
        return ianTaskPollingInterval;
    }

    public void setIanTaskPollingInterval(Duration ianTaskPollingInterval) {
        this.ianTaskPollingInterval = ianTaskPollingInterval;
    }

    public int getIanTaskFetchSize() {
        return ianTaskFetchSize;
    }

    public void setIanTaskFetchSize(int ianTaskFetchSize) {
        this.ianTaskFetchSize = greaterZero(ianTaskFetchSize, "ianTaskFetchSize");
    }

    public String getFallbackCMoveSCP() {
        return fallbackCMoveSCP;
    }

    public void setFallbackCMoveSCP(String fallbackCMoveSCP) {
        this.fallbackCMoveSCP = fallbackCMoveSCP;
    }

    public String getFallbackCMoveSCPDestination() {
        return fallbackCMoveSCPDestination;
    }

    public void setFallbackCMoveSCPDestination(String fallbackCMoveSCPDestination) {
        this.fallbackCMoveSCPDestination = fallbackCMoveSCPDestination;
    }

    public String getFallbackCMoveSCPLeadingCFindSCP() {
        return fallbackCMoveSCPLeadingCFindSCP;
    }

    public void setFallbackCMoveSCPLeadingCFindSCP(String fallbackCMoveSCPLeadingCFindSCP) {
        this.fallbackCMoveSCPLeadingCFindSCP = fallbackCMoveSCPLeadingCFindSCP;
    }

    public int getFallbackCMoveSCPRetries() {
        return fallbackCMoveSCPRetries;
    }

    public void setFallbackCMoveSCPRetries(int fallbackCMoveSCPRetries) {
        this.fallbackCMoveSCPRetries = fallbackCMoveSCPRetries;
    }

    public String getExternalRetrieveAEDestination() {
        return externalRetrieveAEDestination;
    }

    public void setExternalRetrieveAEDestination(String externalRetrieveAEDestination) {
        this.externalRetrieveAEDestination = externalRetrieveAEDestination;
    }

    public String getAlternativeCMoveSCP() {
        return alternativeCMoveSCP;
    }

    public void setAlternativeCMoveSCP(String alternativeCMoveSCP) {
        this.alternativeCMoveSCP = alternativeCMoveSCP;
    }

    public int getQidoMaxNumberOfResults() {
        return qidoMaxNumberOfResults;
    }

    public void setQidoMaxNumberOfResults(int qidoMaxNumberOfResults) {
        this.qidoMaxNumberOfResults = qidoMaxNumberOfResults;
    }

    public int getExportTaskFetchSize() {
        return exportTaskFetchSize;
    }

    public void setExportTaskFetchSize(int exportTaskFetchSize) {
        this.exportTaskFetchSize = greaterZero(exportTaskFetchSize, "exportTaskFetchSize");
    }

    public Duration getExportTaskPollingInterval() {
        return exportTaskPollingInterval;
    }

    public void setExportTaskPollingInterval(Duration exportTaskPollingInterval) {
        this.exportTaskPollingInterval = exportTaskPollingInterval;
    }

    public Duration getDeleteRejectedPollingInterval() {
        return deleteRejectedPollingInterval;
    }

    public void setDeleteRejectedPollingInterval(Duration deleteRejectedPollingInterval) {
        this.deleteRejectedPollingInterval = deleteRejectedPollingInterval;
    }

    public int getDeleteRejectedFetchSize() {
        return deleteRejectedFetchSize;
    }

    public void setDeleteRejectedFetchSize(int deleteRejectedFetchSize) {
        this.deleteRejectedFetchSize =  greaterZero(deleteRejectedFetchSize, "deleteRejectedFetchSize");

    }
    public Duration getPurgeStoragePollingInterval() {
        return purgeStoragePollingInterval;
    }

    public void setPurgeStoragePollingInterval(Duration purgeStoragePollingInterval) {
        this.purgeStoragePollingInterval = purgeStoragePollingInterval;
    }

    public int getPurgeStorageFetchSize() {
        return purgeStorageFetchSize;
    }

    public void setPurgeStorageFetchSize(int purgeStorageFetchSize) {
        this.purgeStorageFetchSize = greaterZero(purgeStorageFetchSize, "purgeStorageFetchSize");
    }

    public int getDeleteStudyBatchSize() {
        return deleteStudyBatchSize;
    }

    public void setDeleteStudyBatchSize(int deleteStudyBatchSize) {
        this.deleteStudyBatchSize = greaterZero(deleteStudyBatchSize, "deleteStudyBatchSize");
    }

    public boolean isDeletePatientOnDeleteLastStudy() {
        return deletePatientOnDeleteLastStudy;
    }

    public void setDeletePatientOnDeleteLastStudy(boolean deletePatientOnDeleteLastStudy) {
        this.deletePatientOnDeleteLastStudy = deletePatientOnDeleteLastStudy;
    }

    public Duration getMaxAccessTimeStaleness() {
        return maxAccessTimeStaleness;
    }

    public void setMaxAccessTimeStaleness(Duration maxAccessTimeStaleness) {
        this.maxAccessTimeStaleness = maxAccessTimeStaleness;
    }

    public Duration getAECacheStaleTimeout() {
        return aeCacheStaleTimeout;
    }

    public void setAECacheStaleTimeout(Duration aeCacheStaleTimeout) {
        this.aeCacheStaleTimeout = aeCacheStaleTimeout;
    }

    public int getAECacheStaleTimeoutSeconds() {
        return toSeconds(aeCacheStaleTimeout);
    }

    public Duration getLeadingCFindSCPQueryCacheStaleTimeout() {
        return leadingCFindSCPQueryCacheStaleTimeout;
    }

    public void setLeadingCFindSCPQueryCacheStaleTimeout(Duration leadingCFindSCPQueryCacheStaleTimeout) {
        this.leadingCFindSCPQueryCacheStaleTimeout = leadingCFindSCPQueryCacheStaleTimeout;
    }

    public int getLeadingCFindSCPQueryCacheStaleTimeoutSeconds() {
        return toSeconds(leadingCFindSCPQueryCacheStaleTimeout);
    }

    private static int toSeconds(Duration timeout) {
        return timeout != null ? (int) timeout.getSeconds() : 0;
    }

    public int getLeadingCFindSCPQueryCacheSize() {
        return leadingCFindSCPQueryCacheSize;
    }

    public void setLeadingCFindSCPQueryCacheSize(int leadingCFindSCPQueryCacheSize) {
        this.leadingCFindSCPQueryCacheSize =
                greaterZero(leadingCFindSCPQueryCacheSize, "leadingCFindSCPQueryCacheSize");
    }

    public String getAuditSpoolDirectory() {
        return auditSpoolDirectory;
    }

    public void setAuditSpoolDirectory(String auditSpoolDirectory) {
        this.auditSpoolDirectory = auditSpoolDirectory;
    }

    public Duration getAuditPollingInterval() {
        return auditPollingInterval;
    }

    public void setAuditPollingInterval(Duration auditPollingInterval) {
        this.auditPollingInterval = auditPollingInterval;
    }

    public Duration getAuditAggregateDuration() {
        return auditAggregateDuration;
    }

    public void setAuditAggregateDuration(Duration auditAggregateDuration) {
        this.auditAggregateDuration = auditAggregateDuration;
    }

    public boolean isAuditAggregate() {
        return auditSpoolDirectory != null && auditPollingInterval != null && auditAggregateDuration != null;
    }

    public String getStowSpoolDirectory() {
        return stowSpoolDirectory;
    }

    public void setStowSpoolDirectory(String stowSpoolDirectory) {
        this.stowSpoolDirectory = stowSpoolDirectory;
    }

    public String getWadoSpoolDirectory() {
        return wadoSpoolDirectory;
    }

    public void setWadoSpoolDirectory(String wadoSpoolDirectory) {
        this.wadoSpoolDirectory = wadoSpoolDirectory;
    }

    public String getHl7LogFilePattern() {
        return hl7LogFilePattern;
    }

    public void setHl7LogFilePattern(String hl7LogFilePattern) {
        this.hl7LogFilePattern = hl7LogFilePattern;
    }

    public String getHl7ErrorLogFilePattern() {
        return hl7ErrorLogFilePattern;
    }

    public void setHl7ErrorLogFilePattern(String hl7ErrorLogFilePattern) {
        this.hl7ErrorLogFilePattern = hl7ErrorLogFilePattern;
    }

    public int getRejectExpiredStudiesFetchSize() {
        return rejectExpiredStudiesFetchSize;
    }

    public void setRejectExpiredStudiesFetchSize(int rejectExpiredStudiesFetchSize) {
        this.rejectExpiredStudiesFetchSize =
                greaterZero(rejectExpiredStudiesFetchSize, "rejectExpiredStudiesFetchSize");
    }

    public int getRejectExpiredSeriesFetchSize() {
        return rejectExpiredSeriesFetchSize;
    }

    public void setRejectExpiredSeriesFetchSize(int rejectExpiredSeriesFetchSize) {
        this.rejectExpiredSeriesFetchSize =
                greaterZero(rejectExpiredSeriesFetchSize, "rejectExpiredSeriesFetchSize");;
    }

    public Duration getRejectExpiredStudiesPollingInterval() {
        return rejectExpiredStudiesPollingInterval;
    }

    public void setRejectExpiredStudiesPollingInterval(Duration rejectExpiredStudiesPollingInterval) {
        this.rejectExpiredStudiesPollingInterval = rejectExpiredStudiesPollingInterval;
    }

    public LocalTime getRejectExpiredStudiesPollingStartTime() {
        return rejectExpiredStudiesPollingStartTime;
    }

    public void setRejectExpiredStudiesPollingStartTime(LocalTime rejectExpiredStudiesPollingStartTime) {
        this.rejectExpiredStudiesPollingStartTime = rejectExpiredStudiesPollingStartTime;
    }

    public String getRejectExpiredStudiesAETitle() {
        return rejectExpiredStudiesAETitle;
    }

    public void setRejectExpiredStudiesAETitle(String rejectExpiredStudiesAETitle) {
        this.rejectExpiredStudiesAETitle = rejectExpiredStudiesAETitle;
    }

    public String getFallbackCMoveSCPStudyOlderThan() {
        return fallbackCMoveSCPStudyOlderThan;
    }

    public void setFallbackCMoveSCPStudyOlderThan(String fallbackCMoveSCPStudyOlderThan) {
        this.fallbackCMoveSCPStudyOlderThan = fallbackCMoveSCPStudyOlderThan;
    }

    public Duration getPurgeQueueMessagePollingInterval() {
        return purgeQueueMessagePollingInterval;
    }

    public void setPurgeQueueMessagePollingInterval(Duration purgeQueueMessagePollingInterval) {
        this.purgeQueueMessagePollingInterval = purgeQueueMessagePollingInterval;
    }

    public Duration getPurgeStgCmtPollingInterval() {
        return purgeStgCmtPollingInterval;
    }

    public void setPurgeStgCmtPollingInterval(Duration purgeStgCmtPollingInterval) {
        this.purgeStgCmtPollingInterval = purgeStgCmtPollingInterval;
    }

    public Duration getPurgeStgCmtCompletedDelay() {
        return purgeStgCmtCompletedDelay;
    }

    public void setPurgeStgCmtCompletedDelay(Duration purgeStgCmtCompletedDelay) {
        this.purgeStgCmtCompletedDelay = purgeStgCmtCompletedDelay;
    }

    public SPSStatus[] getHideSPSWithStatusFrom() {
        return hideSPSWithStatusFrom;
    }

    public void setHideSPSWithStatusFrom(SPSStatus[] hideSPSWithStatusFrom) {
        this.hideSPSWithStatusFrom = hideSPSWithStatusFrom;
    }

    public String getStorePermissionServiceURL() {
        return storePermissionServiceURL;
    }

    public void setStorePermissionServiceURL(String storePermissionServiceURL) {
        this.storePermissionServiceURL = storePermissionServiceURL;
    }

    public Pattern getStorePermissionServiceResponsePattern() {
        return storePermissionServiceResponsePattern;
    }

    public void setStorePermissionServiceResponsePattern(Pattern storePermissionServiceResponsePattern) {
        this.storePermissionServiceResponsePattern = storePermissionServiceResponsePattern;
    }

    public Pattern getStorePermissionServiceExpirationDatePattern() {
        return storePermissionServiceExpirationDatePattern;
    }

    public void setStorePermissionServiceExpirationDatePattern(Pattern storePermissionServiceExpirationDatePattern) {
        this.storePermissionServiceExpirationDatePattern = storePermissionServiceExpirationDatePattern;
    }

    public Pattern getStorePermissionServiceErrorCommentPattern() {
        return storePermissionServiceErrorCommentPattern;
    }

    public void setStorePermissionServiceErrorCommentPattern(Pattern storePermissionServiceErrorCommentPattern) {
        this.storePermissionServiceErrorCommentPattern = storePermissionServiceErrorCommentPattern;
    }

    public Pattern getStorePermissionServiceErrorCodePattern() {
        return storePermissionServiceErrorCodePattern;
    }

    public void setStorePermissionServiceErrorCodePattern(Pattern storePermissionServiceErrorCodePattern) {
        this.storePermissionServiceErrorCodePattern = storePermissionServiceErrorCodePattern;
    }

    public Duration getStorePermissionCacheStaleTimeout() {
        return storePermissionCacheStaleTimeout;
    }

    public void setStorePermissionCacheStaleTimeout(Duration storePermissionCacheStaleTimeout) {
        this.storePermissionCacheStaleTimeout = storePermissionCacheStaleTimeout;
    }

    public int getStorePermissionCacheStaleTimeoutSeconds() {
        return toSeconds(storePermissionCacheStaleTimeout);
    }

    public int getStorePermissionCacheSize() {
        return storePermissionCacheSize;
    }

    public void setStorePermissionCacheSize(int storePermissionCacheSize) {
        this.storePermissionCacheSize = greaterZero(storePermissionCacheSize, "storePermissionCacheSize");
    }

    public Duration getMergeMWLCacheStaleTimeout() {
        return mergeMWLCacheStaleTimeout;
    }

    public void setMergeMWLCacheStaleTimeout(Duration mergeMWLCacheStaleTimeout) {
        this.mergeMWLCacheStaleTimeout = mergeMWLCacheStaleTimeout;
    }

    public int getMergeMWLCacheStaleTimeoutSeconds() {
        return toSeconds(mergeMWLCacheStaleTimeout);
    }

    public int getMergeMWLCacheSize() {
        return mergeMWLCacheSize;
    }

    public void setMergeMWLCacheSize(int mergeMWLCacheSize) {
        this.mergeMWLCacheSize = greaterZero(mergeMWLCacheSize, "mergeMWLCacheSize");
    }

    public int getStoreUpdateDBMaxRetries() {
        return storeUpdateDBMaxRetries;
    }

    public void setStoreUpdateDBMaxRetries(int storeUpdateDBMaxRetries) {
        this.storeUpdateDBMaxRetries = storeUpdateDBMaxRetries;
    }

    public AllowRejectionForDataRetentionPolicyExpired getAllowRejectionForDataRetentionPolicyExpired() {
        return allowRejectionForDataRetentionPolicyExpired;
    }

    public void setAllowRejectionForDataRetentionPolicyExpired(
            AllowRejectionForDataRetentionPolicyExpired allowRejectionForDataRetentionPolicyExpired) {
        this.allowRejectionForDataRetentionPolicyExpired = allowRejectionForDataRetentionPolicyExpired;
    }

    public AttributeFilter getAttributeFilter(Entity entity) {
        AttributeFilter filter = attributeFilters.get(entity);
        if (filter == null)
            throw new IllegalArgumentException("No Attribute Filter for " + entity + " configured");

        return filter;
    }

    public void setAttributeFilter(Entity entity, AttributeFilter filter) {
        attributeFilters.put(entity, filter);
    }

    public Map<Entity, AttributeFilter> getAttributeFilters() {
        return attributeFilters;
    }

    public MetadataFilter getMetadataFilter(String name) {
        return metadataFilters.get(name);
    }

    public void addMetadataFilter(MetadataFilter filter) {
        metadataFilters.put(filter.getName(), filter);
    }

    public void removeMetadataFilter(MetadataFilter filter) {
        metadataFilters.remove(filter.getName());
    }

    public Map<String, MetadataFilter> getMetadataFilters() {
        return metadataFilters;
    }

    public IDGenerator getIDGenerator(IDGenerator.Name name) {
        IDGenerator filter = idGenerators.get(name);
        if (filter == null)
            throw new IllegalArgumentException("No ID Generator for " + name + " configured");

        return filter;
    }

    public void addIDGenerator(IDGenerator generator) {
        idGenerators.put(generator.getName(), generator);
    }

    public void removeIDGenerator(IDGenerator generator) {
        idGenerators.remove(generator.getName());
    }

    public Map<IDGenerator.Name, IDGenerator> getIDGenerators() {
        return idGenerators;
    }

    public QueryRetrieveView getQueryRetrieveView(String viewID) {
        return queryRetrieveViewMap.get(viewID);
    }

    public QueryRetrieveView getQueryRetrieveViewNotNull(String viewID) {
        QueryRetrieveView view = getQueryRetrieveView(viewID);
        if (view == null)
            throw new IllegalArgumentException("No Query Retrieve View configured with ID:" + viewID);
        return view;
    }

    public QueryRetrieveView removeQueryRetrieveView(String viewID) {
        return queryRetrieveViewMap.remove(viewID);
    }

    public void addQueryRetrieveView(QueryRetrieveView view) {
        queryRetrieveViewMap.put(view.getViewID(), view);
    }

    public Collection<QueryRetrieveView> getQueryRetrieveViews() {
        return queryRetrieveViewMap.values();
    }

    public Collection<String> getQueryRetrieveViewIDs() {
        return queryRetrieveViewMap.keySet();
    }

    public StorageDescriptor getStorageDescriptor(String storageID) {
        return storageDescriptorMap.get(storageID);
    }

    public StorageDescriptor getStorageDescriptorNotNull(String storageID) {
        StorageDescriptor descriptor = getStorageDescriptor(storageID);
        if (descriptor == null)
            throw new IllegalArgumentException("No Storage configured with ID:" + storageID);
        return descriptor;
    }

    public StorageDescriptor removeStorageDescriptor(String storageID) {
        return storageDescriptorMap.remove(storageID);
    }

    public void addStorageDescriptor(StorageDescriptor descriptor) {
        storageDescriptorMap.put(descriptor.getStorageID(), descriptor);
    }

    public Collection<StorageDescriptor> getStorageDescriptors() {
        return storageDescriptorMap.values();
    }

    public QueueDescriptor getQueueDescriptor(String queueName) {
        return queueDescriptorMap.get(queueName);
    }

    public QueueDescriptor getQueueDescriptorNotNull(String queueName) {
        QueueDescriptor descriptor = getQueueDescriptor(queueName);
        if (descriptor == null)
            throw new IllegalArgumentException("No Queue configured with name:" + queueName);
        return descriptor;
    }

    public QueueDescriptor removeQueueDescriptor(String queueName) {
        return queueDescriptorMap.remove(queueName);
    }

    public void addQueueDescriptor(QueueDescriptor descriptor) {
        queueDescriptorMap.put(descriptor.getQueueName(), descriptor);
    }

    public Collection<QueueDescriptor> getQueueDescriptors() {
        return queueDescriptorMap.values();
    }

    public ExporterDescriptor getExporterDescriptor(String exporterID) {
        return exporterDescriptorMap.get(exporterID);
    }

    public ExporterDescriptor getExporterDescriptorNotNull(String exporterID) {
        ExporterDescriptor descriptor = getExporterDescriptor(exporterID);
        if (descriptor == null)
            throw new IllegalArgumentException("No Exporter configured with ID:" + exporterID);
        return descriptor;
    }

    public ExporterDescriptor removeExporterDescriptor(String exporterID) {
        return exporterDescriptorMap.remove(exporterID);
    }

    public void addExporterDescriptor(ExporterDescriptor destination) {
        exporterDescriptorMap.put(destination.getExporterID(), destination);
    }

    private int greaterZero(int i, String prompt) {
        if (i <= 0)
            throw new IllegalArgumentException(prompt + ": " + i);
        return i;
    }

    public Collection<ExporterDescriptor> getExporterDescriptors() {
        return exporterDescriptorMap.values();
    }

    public void removeExportRule(ExportRule rule) {
        exportRules.remove(rule);
    }

    public void clearExportRules() {
        exportRules.clear();
    }

    public void addExportRule(ExportRule rule) {
        exportRules.add(rule);
    }

    public Collection<ExportRule> getExportRules() {
        return exportRules;
    }

    public void removeRSForwardRule(RSForwardRule rule) {
        rsForwardRules.remove(rule);
    }

    public void clearRSForwardRules() {
        rsForwardRules.clear();
    }

    public void addRSForwardRule(RSForwardRule rule) {
        rsForwardRules.add(rule);
    }

    public Collection<RSForwardRule> getRSForwardRules() {
        return rsForwardRules;
    }

    public void removeHL7ForwardRule(HL7ForwardRule rule) {
        hl7ForwardRules.remove(rule);
    }

    public void clearHL7ForwardRules() {
        hl7ForwardRules.clear();
    }

    public void addHL7ForwardRule(HL7ForwardRule rule) {
        hl7ForwardRules.add(rule);
    }

    public Collection<HL7ForwardRule> getHL7ForwardRules() {
        return hl7ForwardRules;
    }

    public void removeCompressionRule(ArchiveCompressionRule rule) {
        compressionRules.remove(rule);
    }

    public void clearCompressionRules() {
        compressionRules.clear();
    }

    public void addCompressionRule(ArchiveCompressionRule rule) {
        compressionRules.add(rule);
    }

    public Collection<ArchiveCompressionRule> getCompressionRules() {
        return compressionRules;
    }

    public void removeStudyRetentionPolicy(StudyRetentionPolicy policy) {
        studyRetentionPolicies.remove(policy);
    }

    public void clearStudyRetentionPolicies() {
        studyRetentionPolicies.clear();
    }

    public void addStudyRetentionPolicy(StudyRetentionPolicy policy) {
        studyRetentionPolicies.add(policy);
    }

    public Collection<StudyRetentionPolicy> getStudyRetentionPolicies() {
        return studyRetentionPolicies;
    }

    public void removeAttributeCoercion(ArchiveAttributeCoercion coercion) {
        attributeCoercions.remove(coercion);
    }

    public void clearAttributeCoercions() {
        attributeCoercions.clear();
    }

    public void addAttributeCoercion(ArchiveAttributeCoercion coercion) {
        attributeCoercions.add(coercion);
    }

    public Collection<ArchiveAttributeCoercion> getAttributeCoercions() {
        return attributeCoercions;
    }

    public void removeStoreAccessControlIDRule(StoreAccessControlIDRule storeAccessControlIDRule) {
        storeAccessControlIDRules.remove(storeAccessControlIDRule);
    }

    public void clearStoreAccessControlIDRules() {
        storeAccessControlIDRules.clear();
    }

    public void addStoreAccessControlIDRule(StoreAccessControlIDRule storeAccessControlIDRule) {
        storeAccessControlIDRules.add(storeAccessControlIDRule);
    }

    public ArrayList<StoreAccessControlIDRule> getStoreAccessControlIDRules() {
        return storeAccessControlIDRules;
    }

    public RejectionNote getRejectionNote(String rjNoteID) {
        return rejectionNoteMap.get(rjNoteID);
    }

    public RejectionNote getRejectionNote(Code code) {
        if (code != null)
            for (RejectionNote rjNote : rejectionNoteMap.values()) {
                if (rjNote.getRejectionNoteCode().equalsIgnoreMeaning(code))
                    return rjNote;
            }
        return null;
    }

    public RejectionNote getRejectionNote(RejectionNote.Type rejectionNoteType) {
        for (RejectionNote rejectionNote : rejectionNoteMap.values()) {
            if (rejectionNote.getRejectionNoteType() == rejectionNoteType)
                return rejectionNote;
        }
        return null;
    }

    public RejectionNote removeRejectionNote(String rjNoteID) {
        return rejectionNoteMap.remove(rjNoteID);
    }

    public void addRejectionNote(RejectionNote rjNote) {
        rejectionNoteMap.put(rjNote.getRejectionNoteLabel(), rjNote);
    }

    public Collection<RejectionNote> getRejectionNotes() {
        return rejectionNoteMap.values();
    }

    public AllowDeleteStudyPermanently getAllowDeleteStudyPermanently() {
        return allowDeleteStudyPermanently;
    }

    public void setAllowDeleteStudyPermanently(AllowDeleteStudyPermanently allowDeleteStudyPermanently) {
        this.allowDeleteStudyPermanently = allowDeleteStudyPermanently;
    }

    public String[] getRetrieveAETitles() {
        return retrieveAETitles;
    }

    public void setRetrieveAETitles(String... retrieveAETitles) {
        this.retrieveAETitles = retrieveAETitles;
    }

    @Override
    public void reconfigure(DeviceExtension from) {
        ArchiveDeviceExtension arcdev = (ArchiveDeviceExtension) from;
        defaultCharacterSet = arcdev.defaultCharacterSet;
        fuzzyAlgorithmClass = arcdev.fuzzyAlgorithmClass;
        fuzzyStr = arcdev.fuzzyStr;
        storageID = arcdev.storageID;
        metadataStorageID = arcdev.metadataStorageID;
        seriesMetadataStorageID = arcdev.seriesMetadataStorageID;
        seriesMetadataDelay = arcdev.seriesMetadataDelay;
        seriesMetadataPollingInterval = arcdev.seriesMetadataPollingInterval;
        seriesMetadataFetchSize = arcdev.seriesMetadataFetchSize;
        purgeInstanceRecordsDelay = arcdev.purgeInstanceRecordsDelay;
        purgeInstanceRecordsPollingInterval = arcdev.purgeInstanceRecordsPollingInterval;
        purgeInstanceRecordsFetchSize = arcdev.purgeInstanceRecordsFetchSize;
        overwritePolicy = arcdev.overwritePolicy;
        showPatientInfoInSystemLog = arcdev.showPatientInfoInSystemLog;
        showPatientInfoInAuditLog = arcdev.showPatientInfoInAuditLog;
        bulkDataSpoolDirectory = arcdev.bulkDataSpoolDirectory;
        queryRetrieveViewID = arcdev.queryRetrieveViewID;
        personNameComponentOrderInsensitiveMatching = arcdev.personNameComponentOrderInsensitiveMatching;
        sendPendingCGet = arcdev.sendPendingCGet;
        sendPendingCMoveInterval = arcdev.sendPendingCMoveInterval;
        wadoSupportedSRClasses.clear();
        wadoSupportedSRClasses.addAll(arcdev.wadoSupportedSRClasses);
        wadoSR2HtmlTemplateURI = arcdev.wadoSR2HtmlTemplateURI;
        wadoSR2TextTemplateURI = arcdev.wadoSR2TextTemplateURI;
        patientUpdateTemplateURI = arcdev.patientUpdateTemplateURI;
        importReportTemplateURI = arcdev.importReportTemplateURI;
        scheduleProcedureTemplateURI = arcdev.scheduleProcedureTemplateURI;
        qidoMaxNumberOfResults = arcdev.qidoMaxNumberOfResults;
        queryRetrieveViewMap.clear();
        queryRetrieveViewMap.putAll(arcdev.queryRetrieveViewMap);
        mppsForwardDestinations = arcdev.mppsForwardDestinations;
        ianDestinations = arcdev.ianDestinations;
        ianDelay = arcdev.ianDelay;
        ianTimeout = arcdev.ianTimeout;
        ianOnTimeout = arcdev.ianOnTimeout;
        ianTaskPollingInterval = arcdev.ianTaskPollingInterval;
        ianTaskFetchSize = arcdev.ianTaskFetchSize;
        fallbackCMoveSCP = arcdev.fallbackCMoveSCP;
        fallbackCMoveSCPDestination = arcdev.fallbackCMoveSCPDestination;
        fallbackCMoveSCPLeadingCFindSCP = arcdev.fallbackCMoveSCPLeadingCFindSCP;
        fallbackCMoveSCPRetries = arcdev.fallbackCMoveSCPRetries;
        externalRetrieveAEDestination = arcdev.externalRetrieveAEDestination;
        alternativeCMoveSCP = arcdev.alternativeCMoveSCP;
        exportTaskPollingInterval = arcdev.exportTaskPollingInterval;
        exportTaskFetchSize = arcdev.exportTaskFetchSize;
        deleteRejectedPollingInterval = arcdev.deleteRejectedPollingInterval;
        deleteRejectedFetchSize = arcdev.deleteRejectedFetchSize;
        purgeStoragePollingInterval = arcdev.purgeStoragePollingInterval;
        purgeStorageFetchSize = arcdev.purgeStorageFetchSize;
        deleteStudyBatchSize = arcdev.deleteStudyBatchSize;
        deletePatientOnDeleteLastStudy = arcdev.deletePatientOnDeleteLastStudy;
        maxAccessTimeStaleness = arcdev.maxAccessTimeStaleness;
        aeCacheStaleTimeout = arcdev.aeCacheStaleTimeout;
        leadingCFindSCPQueryCacheStaleTimeout = arcdev.leadingCFindSCPQueryCacheStaleTimeout;
        leadingCFindSCPQueryCacheSize = arcdev.leadingCFindSCPQueryCacheSize;
        auditSpoolDirectory = arcdev.auditSpoolDirectory;
        auditPollingInterval = arcdev.auditPollingInterval;
        auditAggregateDuration = arcdev.auditAggregateDuration;
        stowSpoolDirectory = arcdev.stowSpoolDirectory;
        wadoSpoolDirectory = arcdev.wadoSpoolDirectory;
        hl7LogFilePattern = arcdev.hl7LogFilePattern;
        hl7ErrorLogFilePattern = arcdev.hl7ErrorLogFilePattern;
        purgeQueueMessagePollingInterval = arcdev.purgeQueueMessagePollingInterval;
        purgeStgCmtPollingInterval = arcdev.purgeStgCmtPollingInterval;
        purgeStgCmtCompletedDelay = arcdev.purgeStgCmtCompletedDelay;
        hideSPSWithStatusFrom = arcdev.hideSPSWithStatusFrom;
        rejectExpiredStudiesPollingInterval = arcdev.rejectExpiredStudiesPollingInterval;
        rejectExpiredStudiesPollingStartTime = arcdev.rejectExpiredStudiesPollingStartTime;
        rejectExpiredStudiesFetchSize = arcdev.rejectExpiredStudiesFetchSize;
        rejectExpiredSeriesFetchSize = arcdev.rejectExpiredSeriesFetchSize;
        rejectExpiredStudiesAETitle = arcdev.rejectExpiredStudiesAETitle;
        fallbackCMoveSCPStudyOlderThan = arcdev.fallbackCMoveSCPStudyOlderThan;
        storePermissionServiceURL = arcdev.storePermissionServiceURL;
        storePermissionServiceResponsePattern = arcdev.storePermissionServiceResponsePattern;
        storePermissionServiceExpirationDatePattern = arcdev.storePermissionServiceExpirationDatePattern;
        storePermissionServiceErrorCommentPattern = arcdev.storePermissionServiceErrorCommentPattern;
        storePermissionServiceErrorCodePattern = arcdev.storePermissionServiceErrorCodePattern;
        storePermissionCacheStaleTimeout = arcdev.storePermissionCacheStaleTimeout;
        storePermissionCacheSize = arcdev.storePermissionCacheSize;
        mergeMWLCacheStaleTimeout = arcdev.mergeMWLCacheStaleTimeout;
        mergeMWLCacheSize = arcdev.mergeMWLCacheSize;
        storeUpdateDBMaxRetries = arcdev.storeUpdateDBMaxRetries;
        allowRejectionForDataRetentionPolicyExpired = arcdev.allowRejectionForDataRetentionPolicyExpired;
        acceptMissingPatientID = arcdev.acceptMissingPatientID;
        allowDeleteStudyPermanently = arcdev.allowDeleteStudyPermanently;
        retrieveAETitles = arcdev.retrieveAETitles;
        attributeFilters.clear();
        attributeFilters.putAll(arcdev.attributeFilters);
        metadataFilters.clear();
        metadataFilters.putAll(arcdev.metadataFilters);
        idGenerators.clear();
        idGenerators.putAll(arcdev.idGenerators);
        storageDescriptorMap.clear();
        storageDescriptorMap.putAll(arcdev.storageDescriptorMap);
        queueDescriptorMap.clear();
        queueDescriptorMap.putAll(arcdev.queueDescriptorMap);
        exporterDescriptorMap.clear();
        exporterDescriptorMap.putAll(arcdev.exporterDescriptorMap);
        exportRules.clear();
        exportRules.addAll(arcdev.exportRules);
        rsForwardRules.clear();
        rsForwardRules.addAll(arcdev.rsForwardRules);
        hl7ForwardRules.clear();
        hl7ForwardRules.addAll(arcdev.hl7ForwardRules);
        compressionRules.clear();
        compressionRules.addAll(arcdev.compressionRules);
        studyRetentionPolicies.clear();
        studyRetentionPolicies.addAll(arcdev.studyRetentionPolicies);
        attributeCoercions.clear();
        attributeCoercions.addAll(arcdev.attributeCoercions);
        storeAccessControlIDRules.clear();
        storeAccessControlIDRules.addAll(arcdev.storeAccessControlIDRules);
        rejectionNoteMap.clear();
        rejectionNoteMap.putAll(arcdev.rejectionNoteMap);
    }
}
