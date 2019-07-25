package com.github.masooh.gocdpicodsl.dsl

data class Stage(val name: String, val manualApproval: Boolean = false) {
    var jobs : MutableList<Job> = mutableListOf()

    fun job(name: String, init: Job.() -> Unit): Job {
        val job = Job(name)
        job.init()
        jobs.add(job)
        return job
    }
}