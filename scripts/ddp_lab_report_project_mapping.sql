-- Change the project_id value on the next line to match the one for your DDP project.
set @project_id = 352;

-- Do NOT change anything below this line.
delete from redcap_ddp_mapping where project_id = @project_id;
delete from redcap_ddp_preview_fields where project_id = @project_id;
set @event_id = (select event_id from redcap_events_metadata m, redcap_events_arms a where a.arm_id = m.arm_id and a.project_id = @project_id order by a.arm_num, m.day_offset, m.descrip limit 1);
INSERT INTO redcap_ddp_preview_fields (project_id, field1, field2, field3, field4, field5) VALUES
(@project_id, 'national_id', 'patient_first_name', 'patient_surname', 'sex', NULL);
INSERT INTO redcap_ddp_mapping (external_source_field_name, is_record_identifier, project_id, event_id, field_name, temporal_field, preselect) VALUES
('specimen_barcode', 1, @project_id, @event_id, 'specimen_barcode', NULL, NULL),
('lab_rec_id', NULL, @project_id, @event_id, 'lab_rec_id', NULL, NULL),
('date_specimen_collected', NULL, @project_id, @event_id, 'date_specimen_collected', NULL, NULL),
('batch_number', NULL, @project_id, @event_id, 'batch_number', NULL, NULL),
('date_dispatched', NULL, @project_id, @event_id, 'date_dispatched', NULL, NULL),
('time_dispatched', NULL, @project_id, @event_id, 'time_dispatched', NULL, NULL),
('receiving_lab', NULL, @project_id, @event_id, 'receiving_lab', NULL, NULL),
('receiving_personnel', NULL, @project_id, @event_id, 'receiving_personnel', NULL, NULL),
('received_datetime', NULL, @project_id, @event_id, 'received_datetime', NULL, NULL),
('receiving_condition_code', NULL, @project_id, @event_id, 'receiving_condition_code', NULL, NULL),
('patient_surname', NULL, @project_id, @event_id, 'patient_surname', NULL, NULL),
('patient_first_name', NULL, @project_id, @event_id, 'patient_first_name', NULL, NULL),
('national_id', NULL, @project_id, @event_id, 'national_id', NULL, NULL),
('gis_lat', NULL, @project_id, @event_id, 'gis_lat', NULL, NULL),
('gis_long', NULL, @project_id, @event_id, 'gis_long', NULL, NULL),
('date_birth', NULL, @project_id, @event_id, 'date_birth', NULL, NULL),
('sex', NULL, @project_id, @event_id, 'sex', NULL, NULL),
('test_tpor_barcode', NULL, @project_id, @event_id, 'test_tpor_barcode', NULL, NULL),
('test_tpor_id', NULL, @project_id, @event_id, 'test_tpor_id', NULL, NULL),
('tpor_batch_pos', NULL, @project_id, @event_id, 'tpor_batch_pos', NULL, NULL),
('test_tpor_batch_id', NULL, @project_id, @event_id, 'test_tpor_batch_id', NULL, NULL),
('tpor_lab', NULL, @project_id, @event_id, 'tpor_lab', NULL, NULL),
('test_tpor_personnel', NULL, @project_id, @event_id, 'test_tpor_personnel', NULL, NULL),
('test_tpor_datetime', NULL, @project_id, @event_id, 'test_tpor_datetime', NULL, NULL),
('test_tpor_batchsize', NULL, @project_id, @event_id, 'test_tpor_batchsize', NULL, NULL),
('test_ext_barcode', NULL, @project_id, @event_id, 'test_ext_barcode', NULL, NULL),
('test_ext_id', NULL, @project_id, @event_id, 'test_ext_id', NULL, NULL),
('test_ext_batch_id', NULL, @project_id, @event_id, 'test_ext_batch_id', NULL, NULL),
('ext_batch_pos', NULL, @project_id, @event_id, 'ext_batch_pos', NULL, NULL),
('extraction_lab', NULL, @project_id, @event_id, 'extraction_lab', NULL, NULL),
('test_ext_personnel', NULL, @project_id, @event_id, 'test_ext_personnel', NULL, NULL),
('test_ext_datetime', NULL, @project_id, @event_id, 'test_ext_datetime', NULL, NULL),
('test_ext_instrument', NULL, @project_id, @event_id, 'test_ext_instrument', NULL, NULL),
('test_ext_instrument_other', NULL, @project_id, @event_id, 'test_ext_instrument_other', NULL, NULL),
('test_ext_batchsize', NULL, @project_id, @event_id, 'test_ext_batchsize', NULL, NULL),
('test_det_barcode', NULL, @project_id, @event_id, 'test_det_barcode', NULL, NULL),
('test_det_id', NULL, @project_id, @event_id, 'test_det_id', NULL, NULL),
('det_batch_pos', NULL, @project_id, @event_id, 'det_batch_pos', NULL, NULL),
('test_det_batch_id', NULL, @project_id, @event_id, 'test_det_batch_id', NULL, NULL),
('detection_lab', NULL, @project_id, @event_id, 'detection_lab', NULL, NULL),
('test_det_personnel', NULL, @project_id, @event_id, 'test_det_personnel', NULL, NULL),
('test_det_datetime', NULL, @project_id, @event_id, 'test_det_datetime', NULL, NULL),
('test_det_instrument', NULL, @project_id, @event_id, 'test_det_instrument', NULL, NULL),
('test_det_instrument_other', NULL, @project_id, @event_id, 'test_det_instrument_other', NULL, NULL),
('test_det_batchsize', NULL, @project_id, @event_id, 'test_det_batchsize', NULL, NULL),
('test_assay_batch_id', NULL, @project_id, @event_id, 'test_assay_batch_id', NULL, NULL),
('test_assay_personnel', NULL, @project_id, @event_id, 'test_assay_personnel', NULL, NULL),
('test_assay_datetime', NULL, @project_id, @event_id, 'test_assay_datetime', NULL, NULL),
('test_assay_batchsize', NULL, @project_id, @event_id, 'test_assay_batchsize', NULL, NULL),
('test_assay_result', NULL, @project_id, @event_id, 'test_assay_result', NULL, NULL),
('test_assay_result_why', NULL, @project_id, @event_id, 'test_assay_result_why', NULL, NULL),
('test_verify_batch_id', NULL, @project_id, @event_id, 'test_verify_batch_id', NULL, NULL),
('test_verify_personnel', NULL, @project_id, @event_id, 'test_verify_personnel', NULL, NULL),
('test_verify_datetime', NULL, @project_id, @event_id, 'test_verify_datetime', NULL, NULL),
('test_verify_result', NULL, @project_id, @event_id, 'test_verify_result', NULL, NULL),
('result_authorised', NULL, @project_id, @event_id, 'result_authorised', NULL, NULL),
('Result authorised?', NULL, @project_id, @event_id, 'Result authorised?', NULL, NULL),
('authorizer_personnel', NULL, @project_id, @event_id, 'authorizer_personnel', NULL, NULL),
('dispatch_facility', NULL, @project_id, @event_id, 'dispatch_facility', NULL, NULL),
('patient_facility', NULL, @project_id, @event_id, 'patient_facility', NULL, NULL),
('authorizer_datetime', NULL, @project_id, @event_id, 'authorizer_datetime', NULL, NULL),
('ipms_lab_covid_number', NULL, @project_id, @event_id, 'ipms_lab_covid_number', NULL, NULL),
('covid_rna_results', NULL, @project_id, @event_id, 'covid_rna_results', NULL, NULL);
