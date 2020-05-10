-- Change the project_id value on the next line to match the one for your DDP project.
set @project_id = 16;

-- Do NOT change anything below this line.
delete from redcap_ddp_mapping where project_id = @project_id;
delete from redcap_ddp_preview_fields where project_id = @project_id;
set @event_id = (select event_id from redcap_events_metadata m, redcap_events_arms a where a.arm_id = m.arm_id and a.project_id = @project_id order by a.arm_num, m.day_offset, m.descrip limit 1);
INSERT INTO redcap_ddp_preview_fields (project_id, field1, field2, field3, field4, field5) VALUES
(@project_id, 'specimen_dispatcher', 'patient_first_name', 'patient_surname', 'date_birth', 'identity_no');
INSERT INTO redcap_ddp_mapping (external_source_field_name, is_record_identifier, project_id, event_id, field_name, temporal_field, preselect) VALUES
('specimen_barcode', 1, @project_id, @event_id, 'specimen_barcode', NULL, NULL),
('identity_no', NULL, @project_id, @event_id, 'identity_no', NULL, NULL),
('date_birth', NULL, @project_id, @event_id, 'date_birth', NULL, NULL),
('patient_first_name', NULL, @project_id, @event_id, 'patient_first_name', NULL, NULL),
('sex', NULL, @project_id, @event_id, 'sex', NULL, NULL),
('patient_surname', NULL, @project_id, @event_id, 'patient_surname', NULL, NULL),
('patient_contact', NULL, @project_id, @event_id, 'patient_contact', NULL, NULL),
('patient_nationality', NULL, @project_id, @event_id, 'patient_nationality', NULL, NULL),
('patient_city', NULL, @project_id, @event_id, 'patient_city', null, NULL),
('patient_departure_country', NULL, @project_id, @event_id, 'patient_departure_country', NULL, NULL),
('patient_kin', NULL, @project_id, @event_id, 'patient_kin', null, NULL),
('patient_kin_contact', NULL, @project_id, @event_id, 'patient_kin_contact', null, NULL),
('patient_transport_registration', NULL, @project_id, @event_id, 'patient_transport_registration', NULL, NULL),
('patient_departure_date', NULL, @project_id, @event_id, 'patient_departure_date', NULL, NULL),
('batch_number', NULL, @project_id, @event_id, 'batch_number', NULL, NULL),
('date_dispatched', NULL, @project_id, @event_id, 'date_dispatched', NULL, NULL),
('time_dispatched', NULL, @project_id, @event_id, 'time_dispatched', NULL, NULL),
('specimen_dispatcher', NULL, @project_id, @event_id, 'specimen_dispatcher', NULL, NULL),
('specimen_status_dispatch', NULL, @project_id, @event_id, 'specimen_status_dispatch', NULL, NULL),
('location_of_dispatch', NULL, @project_id, @event_id, 'location_of_dispatch', NULL, NULL),
('specimen_dispature_city', NULL, @project_id, @event_id, 'specimen_dispature_city', NULL, NULL),
('specimen_dispatcher_contact', NULL, @project_id, @event_id, 'specimen_dispatcher_contact', NULL, NULL),
('specimen_dispatcher_email', NULL, @project_id, @event_id, 'specimen_dispatcher_email', NULL, NULL),
('receiving_personnel', NULL, @project_id, @event_id, 'receiving_personnel', NULL, NULL),
('receiving_datetime', NULL, @project_id, @event_id, 'receiving_datetime', NULL, NULL),
('receiving_condition_code', NULL, @project_id, @event_id, 'receiving_condition_code', NULL, NULL),
('specimen_results', NULL, @project_id, @event_id, 'specimen_results', NULL, NULL),
('specimen_type', NULL, @project_id, @event_id, 'specimen_type', NULL, NULL),
('date_specimen_collected', NULL, @project_id, @event_id, 'date_specimen_collected', NULL, NULL),
('specimen_test_type', NULL, @project_id, @event_id, 'specimen_test_type', NULL, NULL),
('patient_risk_factors', NULL, @project_id, @event_id, 'patient_risk_factors', NULL, NULL),
('results_entered_by', NULL, @project_id, @event_id, 'results_entered_by', NULL, NULL),
('results_entered_date', NULL, @project_id, @event_id, 'results_entered_date', NULL, NULL),
('results_verifies_by', NULL, @project_id, @event_id, 'results_verifies_by', NULL, NULL),
('results_verified_date', NULL, @project_id, @event_id, 'results_verified_date', NULL, NULL),
('results_authorised_by', NULL, @project_id, @event_id, 'results_authorised_by', NULL, NULL),
('results_authorised_date', NULL, @project_id, @event_id, 'results_authorised_date', NULL, NULL),
('specimen_notes', NULL, @project_id, @event_id, 'specimen_notes', NULL, NULL);

-- ('date_specimen_collected', NULL, @project_id, @event_id, 'date_specimen_collected', 'date_specimen_collected', NULL),
