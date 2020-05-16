DROP TRIGGER IF EXISTS `lab_update_trigger`;

DELIMITER $$
 
create trigger lab_update_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);      
	declare lab_receiving smallint(4);
	declare c_value text;
	declare batchsize smallint(4);
	declare count_ int default 1;
    
	select 352 into lab_report;
	select 362 into lab_receiving;

	-- Stop calling this procedure with any other data
	if (new.field_name = 'covid19_lab_report_complete' and new.value = '2' and new.project_id = lab_report) then
	
		select value into c_value
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'covid_rna_results';
		
		-- update the staging
		update redcap_ddp_specimen
		set dhis2_synched = false
		where specimen_barcode = new.record;
	
	elseif (new.field_name = 'covid_rna_results' and new.project_id = lab_report) then
        
        select value into c_value
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'covid19_lab_report_complete';
					
		if (c_value = '2') then
			-- update the staging
			update redcap_ddp_specimen
			set dhis2_synched = false,
				results = new.value
			where specimen_barcode = new.record;
		end if;
        
	elseif (new.field_name = 'sarscov2_lab_reception_condition_complete ' and new.value = '2' and new.project_id = lab_receiving) then
	
		select value into batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'lab_rec_batchsize';
		
        -- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
        s2_loop:repeat
			
			-- Find the barcode
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('lab_rec_barcode_', count_);
			
			-- Update the staging area
			update redcap_ddp_specimen 
			set dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > batchsize
        end repeat;
		
	elseif (new.field_name = 'sarscov2_lab_reception_complete' and new.value = '2' and new.project_id = lab_receiving) then
		
		select value into batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'lab_rec_batchsize';
            
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
        s2_loop:repeat
			
			-- Find the barcode
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('lab_rec_barcode_', count_);
						
			-- Update the staging area
			update redcap_ddp_specimen 
			set dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > batchsize
        end repeat;
        
	elseif (new.field_name = 'sarscov2_testing_reception_complete' and new.value = '2' and new.project_id = lab_receiving) then
		
		select value into batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_tpor_batchsize';
		
        -- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
        s2_loop:repeat
			
			-- Find the barcode
            select value
            into c_value
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('lab_rec_barcode_', count_);
						
			-- Update the staging area
			update redcap_ddp_specimen 
			set dhis2_synched = false
			where specimen_barcode = c_value;
            
			set count_ = count_ + 1;
        until count_ > batchsize
        end repeat;
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_insert_trigger`;

DELIMITER $$
 
create trigger lab_insert_trigger
after insert
on redcap_data for each row
begin

	declare lab_receiving smallint(4);
	declare lab_report smallint(4);
	declare c_value text;
	declare test_verify_result varchar(4);  
	declare pos varchar(4);	
	  
	select 352 into lab_report;
	select 362 into lab_receiving;

	-- Stop calling this procedure with any other data
	if (new.field_name = 'covid19_lab_report_complete' and new.value = '2' and new.project_id = lab_report) then
	
		select value into test_verify_result
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'result_authorised';
		
		if (test_verify_result = '1') then
			-- update the staging
			update redcap_ddp_specimen
			set dhis2_synched = false
			where specimen_barcode = new.record;
		end if;
		
	elseif (new.field_name = 'result_authorised' and new.value = '1' and new.project_id = lab_report) then
	
		select value into c_value
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'covid_rna_results';
		
		-- update the staging
		update redcap_ddp_specimen
		set dhis2_synched = false
		where specimen_barcode = new.record;
		
	elseif (new.field_name like 'test_assay_result_%' and new.project_id = lab_report) then
		
		-- update the staging
		update redcap_ddp_specimen
		set results_authorised_date = new.value,
			dhis2_synched = false
		where specimen_barcode = new.record;
        			
	elseif (new.field_name = 'covid_rna_results' and new.project_id = lab_report) then
		-- update the staging
		update redcap_ddp_specimen
		set results = new.value,
			dhis2_synched = false
		where specimen_barcode = new.record;
		
	elseif (new.field_name = 'authorizer_datetime' and new.project_id = lab_report) then
		
		-- update the staging
		update redcap_ddp_specimen
		set results_authorised_date = new.value,
			dhis2_synched = false
		where specimen_barcode = new.record;
		
	elseif (new.field_name = 'authorizer_personnel' and new.project_id = lab_report) then
	
		-- update the staging
		update redcap_ddp_specimen
		set results_authorised_by = new.value,
			dhis2_synched = false
		where specimen_barcode = new.record;
		
	elseif (new.field_name like 'lab_rec_barcode_%' and new.project_id = lab_receiving) then
    		
		update redcap_ddp_specimen 
		set dhis2_synched = false
		where specimen_barcode = new.value;
        
	elseif (new.field_name like 'specimen_cond_%' and new.project_id = lab_receiving) then
		
        -- Find the position
		select substring(new.field_name, 15) into pos;
        
        -- Find the barcode
        select value
        into c_value
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = concat('lab_rec_barcode_', pos);
		
        -- Update the staging area
        update redcap_ddp_specimen 
		set dhis2_synched = false
		where specimen_barcode = c_value;
        
	elseif (new.field_name like 'test_tpor_barcode_%' and new.project_id = lab_receiving) then
		
		update redcap_ddp_specimen 
		set dhis2_synched = false
		where specimen_barcode = new.value;
	end if;
		
end; $$

DELIMITER ;
